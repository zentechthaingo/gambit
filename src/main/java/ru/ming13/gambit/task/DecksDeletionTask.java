/*
 * Copyright 2012 Artur Dryomov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ming13.gambit.task;


import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import ru.ming13.gambit.provider.GambitContract;


public class DecksDeletionTask extends AsyncTask<Void, Void, Void>
{
	private final ContentResolver contentResolver;
	private final List<Uri> deckUris;

	public static void execute(ContentResolver contentResolver, List<Uri> deckUris) {
		new DecksDeletionTask(contentResolver, deckUris).execute();
	}

	private DecksDeletionTask(ContentResolver contentResolver, List<Uri> deckUris) {
		this.contentResolver = contentResolver;
		this.deckUris = deckUris;
	}

	@Override
	protected Void doInBackground(Void... parameters) {
		deleteDecks();

		return null;
	}

	private void deleteDecks() {
		try {
			contentResolver.applyBatch(GambitContract.AUTHORITY, buildDeletionOperations());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		} catch (OperationApplicationException e) {
			throw new RuntimeException(e);
		}
	}

	private ArrayList<ContentProviderOperation> buildDeletionOperations() {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (Uri deckUri : deckUris) {
			operations.add(ContentProviderOperation.newDelete(deckUri).build());
		}

		return operations;
	}
}
