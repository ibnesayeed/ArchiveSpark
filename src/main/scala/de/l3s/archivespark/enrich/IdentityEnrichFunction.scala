/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 Helge Holzmann (L3S) and Vinay Goel (Internet Archive)
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

import de.l3s.archivespark.IdentityArchiveRecordField
import de.l3s.archivespark.utils.IdentityMap

class IdentityEnrichFunction[T, Root <: EnrichRoot[_, _]]
(override val dependency: EnrichFunc[Root, _], override val dependencyField: String, val fieldName: String)
  extends DefaultFieldDependentEnrichFunc[Root, Enrichable[T, _], T] with SingleFieldEnrichFunc {

  def this(dependency: DefaultFieldEnrichFunc[Root, _, T], fieldName: String) {
    this(dependency, dependency.defaultField, fieldName)
  }

  override def fields: Seq[String] = Seq(fieldName)
  override def field: IdentityMap[String] = dependency.field.map.find{case (k, v) => v == dependencyField} match {
    case Some((k, v)) => IdentityMap(k -> fieldName)
    case None => IdentityMap()
  }

  override def derive(source: Enrichable[T, _], derivatives: Derivatives): Unit = {
    derivatives << IdentityArchiveRecordField[T]()
  }
}
