package ru.springcoding.prefomega;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

public class GameView extends FrameLayout {
	private PlayingTableView m_playingTable;
    private BetTable m_betTable;
    private ChoiceTable m_choiceTable;

    //private NameView myName;
   // private NameView leftName;
   // private NameView rightName;
    
    
	public GameView(Context _context) {
		super(_context);

        m_playingTable = new PlayingTableView(_context);
      
        addView(m_playingTable);
        
        m_betTable = new BetTable(_context);
        int initTop = m_playingTable.leftClowd.getBottom() + 5 * (PrefApplication.screenHeight) / 480;
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.gravity = Gravity.TOP;
		params.topMargin = initTop;
		params.leftMargin = (PrefApplication.screenWidth - m_betTable.getRawWidth()) / 2;
        addView(m_betTable, params);
		
        m_choiceTable = new ChoiceTable(_context);
		initTop = (2 * PrefApplication.screenHeight) / 3;
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.topMargin = initTop;
		params.leftMargin = (PrefApplication.screenWidth - m_choiceTable.getRawWidth()) / 2;
		addView(m_choiceTable, params);
	
		initNames(_context);
        
		m_betTable.hideBetTable();
		m_choiceTable.hideChoiceTable();
	}
	
	private void initNames(Context _context) {
		/*// own name
		myName = new NameView(_context);
        myName.setText(GameInfo.ownPlayer.name);
        LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.gravity = Gravity.TOP;
		params.topMargin = PrefApplication.screenHeight - GameInfo.myCardsPaddingBottom - GameInfo.cardDimens.y;
		params.leftMargin = GameInfo.myNamePaddingLeft; // TODO  hardcode
		
        this.addView(myName, params); // TODO make own params
        myName.setVisibility(INVISIBLE);
        
        // left name
		leftName = new NameView(_context);
		leftName.setText(GameInfo.nextPlayer.name);
        params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.gravity = Gravity.TOP;
		params.topMargin = GameInfo.otherNamesPaddingTop;
		params.leftMargin = GameInfo.otherNamesPaddingLeft; // TODO  hardcode
		
        this.addView(leftName, params); // TODO make own params
        leftName.setVisibility(INVISIBLE);
        
        // right name
		rightName = new NameView(_context);
		rightName.setText(GameInfo.prevPlayer.name);
        params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.gravity = Gravity.TOP | Gravity.RIGHT;
		params.topMargin = GameInfo.otherNamesPaddingTop;
		params.rightMargin = GameInfo.otherNamesPaddingRight; // TODO  hardcode
		
        this.addView(rightName, params); // TODO make own params
        rightName.setVisibility(INVISIBLE);*/
	}
	
	public void countPaddings() {
		/*GameInfo.myNameWidth = myName.getMeasuredWidth();
		GameInfo.myNameHeight = myName.getMeasuredHeight();
		GameInfo.leftNameWidth = leftName.getMeasuredWidth();
		GameInfo.leftNameHeight = leftName.getMeasuredHeight();
		GameInfo.rightNameWidth = rightName.getMeasuredWidth();
		GameInfo.rightNameHeight = rightName.getMeasuredHeight();
		playingTable.recountOwnCardsPositions();
		playingTable.recountLeftCardsPositions();
		playingTable.recountRightCardsPositions();*/
		GameInfo.paddingsAreCounted = true;
	}


	public void setGameHolder(GameLayout gameHolder) {
		m_playingTable.gameHolder = gameHolder;
	}	
	
	public void updateRoomInfo() {
		/*leftName.setText(GameInfo.nextPlayer.name);
		if (!GameInfo.nextPlayer.name.isEmpty())
			leftName.setVisibility(VISIBLE);
		rightName.setText(GameInfo.prevPlayer.name);
		if (!GameInfo.prevPlayer.name.isEmpty())
			rightName.setVisibility(VISIBLE);
		myName.setText(GameInfo.ownPlayer.name);
		if (!GameInfo.ownPlayer.name.isEmpty())
			myName.setVisibility(VISIBLE);
		rightName.bringToFront();
		myName.bringToFront();
		leftName.bringToFront();
		countPaddings();*/
	}
	
	void showBetTable() {
		m_betTable.showBetTable();
	}
	
	void hideBetTable() {
		m_betTable.hideBetTable();
	}
	
	void hideChoiceTable() {
		m_choiceTable.hideChoiceTable();
	}
	
	void showWhistingTable() {
		m_choiceTable.showWhistingTable();
	}
	
	void showVisOrInvisTable() {
		m_choiceTable.showVisOrInvisTable();
	}
	
	PlayingTableView getPlayingTable() {
		return m_playingTable;
	}
}