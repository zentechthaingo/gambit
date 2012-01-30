package app.android.simpleflashcards.ui;


import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import app.android.simpleflashcards.R;
import app.android.simpleflashcards.SimpleFlashcardsApplication;
import app.android.simpleflashcards.models.Card;
import app.android.simpleflashcards.models.Deck;
import app.android.simpleflashcards.models.ModelsException;


public class CardsListActivity extends SimpleAdapterListActivity
{
	private final Context activityContext = this;

	private static final String LIST_ITEM_FRONT_TEXT = "front_text";
	private static final String LIST_ITEM_BACK_TEXT = "back_text";
	private static final String LIST_ITEM_OBJECT_ID = "object";

	private int deckId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cards);

		processActivityMessage();

		initializeActionbar();
		initializeList();
	}

	@Override
	protected void onResume() {
		super.onResume();

		new LoadCardsTask().execute();
	}

	private void processActivityMessage() {
		deckId = ActivityMessager.getMessageFromActivity(this);
	}

	private void initializeActionbar() {
		ImageButton updateButton = (ImageButton) findViewById(R.id.updateButton);
		updateButton.setOnClickListener(updateListener);

		ImageButton newItemCreationButton = (ImageButton) findViewById(R.id.itemCreationButton);
		newItemCreationButton.setOnClickListener(flashcardCreationListener);
	}

	private OnClickListener updateListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO: Call update task
		}
	};

	private OnClickListener flashcardCreationListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ActivityMessager
				.startActivityWithMessage(activityContext, CardCreationActivity.class, deckId);
		}
	};

	@Override
	protected void initializeList() {
		SimpleAdapter flashcardsAdapter = new SimpleAdapter(activityContext, listData,
			R.layout.cards_list_item, new String[] { LIST_ITEM_FRONT_TEXT, LIST_ITEM_BACK_TEXT },
			new int[] { R.id.title, R.id.description });

		setListAdapter(flashcardsAdapter);

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		registerForContextMenu(getListView());
	}

	private class LoadCardsTask extends AsyncTask<Void, Void, String>
	{
		@Override
		protected void onPreExecute() {
			setEmptyListText(getString(R.string.loadingCards));

			listData.clear();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				Deck deck = SimpleFlashcardsApplication.getInstance().getDecks().getDeckById(deckId);
				addItemsToList(deck.getCardsList());
			}
			catch (ModelsException e) {
				return getString(R.string.someError);
			}

			return new String();
		}

		@Override
		protected void onPostExecute(String result) {
			if (listData.isEmpty()) {
				setEmptyListText(getString(R.string.noCards));
			}
			else {
				updateList();
			}

			if (!result.isEmpty()) {
				UserAlerter.alert(activityContext, result);
			}
		}
	}

	@Override
	protected void addItemToList(Object itemData) {
		Card card = (Card) itemData;

		HashMap<String, Object> cardItem = new HashMap<String, Object>();

		cardItem.put(LIST_ITEM_FRONT_TEXT, card.getFrontSideText());
		cardItem.put(LIST_ITEM_BACK_TEXT, card.getBackSideText());
		cardItem.put(LIST_ITEM_OBJECT_ID, card);

		listData.add(cardItem);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.cards_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo itemInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		int itemPosition = itemInfo.position;

		switch (item.getItemId()) {
			case R.id.editItem:
				new EditCardTask(itemPosition).execute();
				return true;

			case R.id.delete:
				new DeleteCardTask(itemPosition).execute();
				return true;

			default:
				return super.onContextItemSelected(item);
		}
	}

	private class DeleteCardTask extends AsyncTask<Void, Void, String>
	{
		private ProgressDialogShowHelper progressDialogHelper;

		private int cardAdapterPosition;

		public DeleteCardTask(int cardAdapterPosition) {
			super();

			this.cardAdapterPosition = cardAdapterPosition;
		}

		@Override
		protected void onPreExecute() {
			progressDialogHelper = new ProgressDialogShowHelper();
			progressDialogHelper.show(activityContext, getString(R.string.deletingCard));
		}

		@Override
		protected String doInBackground(Void... params) {
			Card card = getCard(cardAdapterPosition);

			try {
				Deck deck = SimpleFlashcardsApplication.getInstance().getDecks().getDeckById(deckId);
				deck.deleteCard(card);
			}
			catch (ModelsException e) {
				return getString(R.string.someError);
			}

			listData.remove(cardAdapterPosition);

			return new String();
		}

		@Override
		protected void onPostExecute(String result) {
			updateList();
			if (listData.isEmpty()) {
				setEmptyListText(getString(R.string.noCards));
			}

			progressDialogHelper.hide();

			if (!result.isEmpty()) {
				UserAlerter.alert(activityContext, result);
			}
		}

	}
	
	private class EditCardTask extends AsyncTask<Void, Void, String>
	{
		private ProgressDialogShowHelper progressDialogHelper;
		
		private int cardId;
		private int cardAdapterPosition;
		
		public EditCardTask(int cardAdapterPosition) {
			super();
			
			this.cardAdapterPosition = cardAdapterPosition;
		}
		
		@Override
		protected void onPreExecute() {
			progressDialogHelper = new ProgressDialogShowHelper();
			progressDialogHelper.show(activityContext, getString(R.string.gettingCardInfo));
		}
		
		@Override
		protected String doInBackground(Void... params) {
			Card card = getCard(cardAdapterPosition);
			
			try {
				cardId = card.getId();
			}
			catch (ModelsException e) {
				return getString(R.string.someError);
			}
			
			return new String();
		}
		
		@Override
		protected void onPostExecute(String result) {
			progressDialogHelper.hide();
			
			if (result.isEmpty()) {
				ActivityMessager.startActivityWithMessage(activityContext, CardEditingActivity.class,
					cardId);
			}
			else {
				UserAlerter.alert(activityContext, result);
			}
		}
	}

	// TODO: Move to super class
	private Card getCard(int adapterPosition) {
		SimpleAdapter listAdapter = (SimpleAdapter) getListAdapter();

		@SuppressWarnings("unchecked")
		Map<String, Object> adapterItem = (Map<String, Object>) listAdapter.getItem(adapterPosition);

		return (Card) adapterItem.get(LIST_ITEM_OBJECT_ID);
	}
}
