/*
 * MIT License
 *
 * Copyright (c) 2023 Hydrologic Engineering Center
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

package cwms.radar.data.dto;

import cwms.radar.api.errors.FieldException;
import java.math.BigDecimal;

public class AssignedTimeSeries implements CwmsDTO
{
	private String timeseriesId;
	private BigDecimal tsCode;

	private String aliasId;
	private String refTsId;
	private Integer attribute;

	public AssignedTimeSeries() {

	}


	public AssignedTimeSeries(String timeseriesId, BigDecimal tsCode, String aliasId, String refTsId, Integer attr)
	{
		this.timeseriesId = timeseriesId;
		this.tsCode = tsCode;
		this.aliasId = aliasId;
		this.refTsId = refTsId;
		this.attribute = attr;
	}

	public String getTimeseriesId()
	{
		return timeseriesId;
	}

	public BigDecimal getTsCode()
	{
		return tsCode;
	}

	public String getAliasId()
	{
		return aliasId;
	}

	public String getRefTsId()
	{
		return refTsId;
	}

	public Integer getAttribute()
	{
		return attribute;
	}

	@Override
	public void validate() throws FieldException {

	}
}
