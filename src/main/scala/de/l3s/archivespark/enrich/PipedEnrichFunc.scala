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

class PipedEnrichFunc[Root <: EnrichRoot[_, _], Source <: Enrichable[_, _]]
(parent: EnrichFunc[Root, Source], override val source: Seq[String])
  extends EnrichFunc[Root, Source] {

  override def derive(source: Source, derivatives: Derivatives): Unit = parent.derive(source, derivatives)

  override def fields: Seq[String] = parent.fields
}

class PipedEnrichFuncWithDefaultField[Root <: EnrichRoot[_, _], Source <: Enrichable[_, _], T]
(parent: DefaultFieldEnrichFunc[Root, Source, T], override val source: Seq[String])
  extends PipedEnrichFunc[Root, Source](parent, source) with DefaultFieldEnrichFunc[Root, Source, T] {
  override def defaultField: String = parent.defaultField
}