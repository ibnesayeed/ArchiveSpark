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

package de.l3s.archivespark.enrich

import de.l3s.archivespark.utils.SelectorUtil

trait DependentEnrichFunc[Root <: EnrichRoot[_, _], Source <: Enrichable[_, _]] extends EnrichFunc[Root, Source] {
  def dependency: EnrichFunc[Root, _]

  def dependencyField: String

  override def source: Seq[String] = dependency.source ++ SelectorUtil.parse(dependencyField).map(f => dependency.field(f))

  def on(dependency: EnrichFunc[Root, _], field: String): EnrichFunc[Root, Source] = new PipedDependentEnrichFunc[Root, Source](this, dependency, field)
  def on(dependency: DefaultFieldEnrichFunc[Root, _, _]): EnrichFunc[Root, Source] = on(dependency, dependency.defaultField)

  def on(dependency: EnrichFunc[Root, _], field: String, index: Int): EnrichFunc[Root, Source] = on(dependency, field + s"[$index]")
  def on(dependency: DefaultFieldEnrichFunc[Root, _, _], index: Int): EnrichFunc[Root, Source] = on(dependency, dependency.defaultField, index)

  def onEach(dependency: EnrichFunc[Root, _], field: String): EnrichFunc[Root, Source] = on(dependency, field + "*")
  def onEach(dependency: DefaultFieldEnrichFunc[Root, _, _]): EnrichFunc[Root, Source] = onEach(dependency, dependency.defaultField)

  def of(dependency: EnrichFunc[Root, _], field: String): EnrichFunc[Root, Source] = on(dependency, field)
  def of(dependency: DefaultFieldEnrichFunc[Root, _, _]): EnrichFunc[Root, Source] = on(dependency)
  def of(dependency: EnrichFunc[Root, _], field: String, index: Int): EnrichFunc[Root, Source] = of(dependency, field, index)
  def of(dependency: DefaultFieldEnrichFunc[Root, _, _], index: Int): EnrichFunc[Root, Source] = of(dependency, index)
  def ofEach(dependency: EnrichFunc[Root, _], field: String): EnrichFunc[Root, Source] = onEach(dependency, field)
  def ofEach(dependency: DefaultFieldEnrichFunc[Root, _, _]): EnrichFunc[Root, Source] = onEach(dependency)

  override protected[enrich] def enrich(root: Root, excludeFromOutput: Boolean): Root = super.enrich(dependency.enrich(root, excludeFromOutput = true), excludeFromOutput)
}
