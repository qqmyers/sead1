/*
 * ShareMedia 
 * Copyright (C) 2005-2006 Nicolas Richeton
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.sharemedia.core;

public interface IConstants {

	String APP_TITLE = "Share Media";

	int[] IMAGE_WIDTH = { 160, 640, 800, 1024 };

	int[] IMAGE_HEIGHT = { 160, 480, 600, 768 };

	int IMAGE_THUMB = 0; // 160x160 max

	int IMAGE_LOW = 1; // 640x480 max

	int IMAGE_STD = 2; // 800x600 max

	int IMAGE_HIGH = 3; // 1024x768 max

	int IMAGE_ORIGINAL = 4; // Full size

	// Metadata ids
	int META_ID = 255;

	int META_DATETIME = 1;

	int META_YEAR = 2;

	int META_MONTH = 6;

	int META_DAY = 7;

	int META_TIME = 3;

	int META_IMAGE_WIDTH = 4;

	int META_IMAGE_HEIGHT = 5;

	int META_AUTHOR = 40;

	int META_KEYWORDS = 41;

	int META_HEADLINE = 42;

	int META_DESCRIPTION = 43;

	int META_PATH = 44;

	int META_VIEW_COUNT = 45;

	int META_ADDITION_DATE = 46;

	int META_MD5 = 47;

	int META_RANK = 48;

	int META_UPDATE_DATE = 49;

	int META_TYPE = 50;

	int META_SIZE = 51;

	int META_NAME = 254;

	// Event is a special metadata ( META_YEAR + META_HEADLINE) that may not be
	// supported by the library. If
	// this is the case, consider META_EVENT = META_HEADLINE
	int META_EVENT = 52;

	int META_EXTENDED_INFORMATION = 255;

	// Settings ids
	int SETTINGS_IMPORT_MOVE = 1;

	int SETTINGS_IMPORT_COPY = 2;

	int SETTINGS_IMPORT_LEAVE = 3;

	// File operation ids
	int ROTATE_CLOCKWISE = 1;

	int ROTATE_COUNTERCLOCKWISE = 2;

	// Media Types
	int TYPE_IMAGE = 0;

	int TYPE_VIDEO = 1;

	int TYPE_AUDIO = 2;

	int NONE = -1;

}
