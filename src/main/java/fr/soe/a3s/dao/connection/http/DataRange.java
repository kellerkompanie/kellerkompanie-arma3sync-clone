/*
   DataRange: String format of block range for http request
   Copyright (C) 2011 Tomas Hlavnika <hlavntom@fel.cvut.cz>

   This file is a part of Jazsync.

   Jazsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2 of the License, or (at
   your option) any later version.

   Jazsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Jazsync; if not, write to the

      Free Software Foundation, Inc.,
      59 Temple Place, Suite 330,
      Boston, MA  02111-1307
      USA
 */

package fr.soe.a3s.dao.connection.http;

/**
 * Block range
 * 
 * @author Tomas Hlavnika
 */
public class DataRange {
	private final long start;
	private final long end;

	public DataRange(long start, long end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Returns range in String format ("start-end"), ready to be put into HTTP
	 * range request
	 * 
	 * @return Range of data in stream
	 */
	public String getRange() {
		if (end == -1) {
			return start + "-";
		} else {
			return start + "-" + end;
		}
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public boolean isPartial() {
		if (start == 0 && end == -1) {
			return false;
		} else {
			return true;
		}
	}
}
