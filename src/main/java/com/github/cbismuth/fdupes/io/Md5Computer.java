/*
 * The MIT License (MIT)
 * Copyright (c) 2016 Christophe Bismuth
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.cbismuth.fdupes.io;

import org.springframework.stereotype.Component;


@Component
public class Md5Computer {

	/*
	 * public String compute(final PathElement pathElement) {
	 * Preconditions.checkNotNull(pathElement, "null file metadata");
	 * 
	 * try (final Timer.Context ignored = getMetricRegistry().timer(name("md5",
	 * "timer")).time()) { return doIt(pathElement); } catch (final Exception e) {
	 * log.error("Can't compute MD5 from file [{}] ([{}]: [{}])",
	 * pathElement.getPath(), e.getClass().getSimpleName(), e.getMessage());
	 * 
	 * return randomUUID().toString(); } }
	 * 
	 * private String doIt(final PathElement element) { try { return new
	 * ProcessExecutor().command(getNativeMd5Command(element)) .readOutput(true)
	 * .execute() .outputString() .split("\\s")[1];
	 * 
	 * return HashingUtils.md5(element);
	 * 
	 * } catch (final Throwable e) { throw new VerifyException(e); } }
	 *
	 *
	 * private Iterable<String> getNativeMd5Command(final PathElement element) {
	 * return newArrayList("openssl", "md5", element.getPath().toString()); }
	 */

}
