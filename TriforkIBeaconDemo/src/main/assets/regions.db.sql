/*
 * Database schema for the RegionHistory database
 * v1		: Initial version
 *
 * @author Christian Melchior <cme@trifork.com>
 */
CREATE TABLE regions (
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	uuid TEXT NOT NULL,
	major INTEGER DEFAULT 0,
	minor INTEGER DEFAULT 0,
	name TEXT NOT NULL,
	enter INTEGER,
	exit INTEGER
);