/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Helge Holzmann (L3S) and Vinay Goel (Internet Archive)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.l3s.archivespark.implicits.classes

import de.l3s.archivespark.ArchiveRecordField
import de.l3s.archivespark.enrich._
import de.l3s.archivespark.implicits._
import de.l3s.archivespark.utils.{SelectorUtil, IdentityMap}
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

class EnrichableRDD[Root <: EnrichRoot[_, _] : ClassTag](rdd: RDD[Root]) {
  def enrich(func: => EnrichFunc[Root, _]): RDD[Root] = rdd.mapPartitions { records =>
    val f = func
    records.map(r => f.enrich(r))
  }

  def mapEnrich[Source, Target](sourceField: String, target: String)(f: Source => Target): RDD[Root] = mapEnrich(SelectorUtil.parse(sourceField), target, target)(f)
  def mapEnrich[Source, Target](sourceField: String, target: String, targetField: String)(f: Source => Target): RDD[Root] = mapEnrich(SelectorUtil.parse(sourceField), target, targetField)(f)
  def mapEnrich[Source, Target](sourceField: Seq[String], target: String)(f: Source => Target): RDD[Root] = mapEnrich(sourceField, target, target)(f)
  def mapEnrich[Source, Target](sourceField: Seq[String], target: String, targetField: String)(f: Source => Target): RDD[Root] = {
    val enrichFunc = new EnrichFunc[Root, Enrichable[Source, _]] {
      override def source: Seq[String] = sourceField
      override def fields: Seq[String] = Seq(target)
      override def field: IdentityMap[String] = IdentityMap(targetField -> target)
      override def derive(source: Enrichable[Source, _], derivatives: Derivatives): Unit = derivatives << ArchiveRecordField(f(source.get))
    }
    rdd.map(r => enrichFunc.enrich(r))
  }

  def mapEnrich[Source, Target](dependencyFunc: DefaultFieldEnrichFunc[Root, _, Source], target: String)(f: Source => Target): RDD[Root] = mapEnrich(dependencyFunc, dependencyFunc.defaultField, target, target)(f)
  def mapEnrich[Source, Target](dependencyFunc: EnrichFunc[Root, _], sourceField: String, target: String)(f: Source => Target): RDD[Root] = mapEnrich(dependencyFunc, sourceField, target, target)(f)
  def mapEnrich[Source, Target](dependencyFunc: EnrichFunc[Root, _], sourceField: String, target: String, targetField: String)(f: Source => Target): RDD[Root] = {
    val enrichFunc = new DependentEnrichFunc[Root, Enrichable[Source, _]] {
      override def dependency: EnrichFunc[Root, _] = dependencyFunc
      override def dependencyField: String = sourceField
      override def fields: Seq[String] = Seq(target)
      override def field: IdentityMap[String] = IdentityMap(targetField -> target)
      override def derive(source: Enrichable[Source, _], derivatives: Derivatives): Unit = derivatives << ArchiveRecordField(f(source.get))
    }
    rdd.map(r => enrichFunc.enrich(r))
  }

  def filterExists(path: String): RDD[Root] = rdd.filter(r => r(path).isDefined)
  def filterExists(f: EnrichFunc[Root, _]): RDD[Root] = rdd.filter(r => f.exists(r))

  def mapPath[T : ClassTag](path: String): RDD[T] = rdd.map(r => r.get[T](path)).filter(o => o.isDefined).map(o => o.get)

  def mapValues[T : ClassTag](path: String): RDD[T] = mapPath(path)

  def mapValues[T : ClassTag](f: DefaultFieldEnrichFunc[Root, _, T]): RDD[T] = rdd.enrich(f).map(r => r.value[T](f)).filter(o => o.isDefined).map(o => o.get)
  def mapValues[T : ClassTag](f: EnrichFunc[Root, _], field: String): RDD[T] = rdd.enrich(f).map(r => r.value[T](f, field)).filter(o => o.isDefined).map(o => o.get)
  def mapMultiValues[T : ClassTag](f: DefaultFieldEnrichFunc[Root, _, T]): RDD[Array[T]] = rdd.enrich(f).map(r => r.values[T](f)).filter(o => o.isDefined).map(o => o.get)
  def mapMultiValues[T : ClassTag](f: EnrichFunc[Root, _], field: String): RDD[Array[T]] = rdd.enrich(f).map(r => r.values[T](f, field)).filter(o => o.isDefined).map(o => o.get)
}
