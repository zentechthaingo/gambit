package app.android.gambit.remote;


import java.util.ArrayList;
import java.util.List;

import com.google.api.client.util.Key;


class SpreadsheetFeed
{
	@Key("entry")
	private List<SpreadsheetEntry> entries;

	public SpreadsheetFeed() {
		entries = new ArrayList<SpreadsheetEntry>();
	}

	public List<SpreadsheetEntry> getEntries() {
		return entries;
	}
}