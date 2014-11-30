package ru.springcoding.prefomega;

import ru.springcoding.prefomega.PlayingCard.SIDE;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;


public class PlayingTableView extends SurfaceView {

private static final int INVALID_POINTER_ID = -1;
	int cardNumber;
	float left;
	float top;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    
    private PlayingCard[] ownCards;
    private PlayingCard[] nextCards;
    private PlayingCard[] prevCards;
    private PlayingCard[] talonCards;
    
    private PlayingCard cardMoveFromNext;
    private PlayingCard cardMoveFromPrev;
    private PlayingCard cardMoveFromOwn;
    private PlayingCard[] threeCards;
    
    private PlayingCard movingCard;
    private int movingCardIndex;
    private GameInfo gameInfo;
    private SurfaceHolder holder;
    private GameLoopThread gameLoopThread;
    
    
    public int talonShowTimer;
    public GameActivity gameActivity;
    TalkClowd ownClowd;
    TalkClowd leftClowd;
    TalkClowd rightClowd;
    GameLayout gameHolder;
    
    int temp;
	DrawState drawState;
	
	NameView myName;
	NameView nextName;
	NameView prevName;
	
	enum DrawState {
		WAITING_FOR_MORE_PLAYERS,
		ARE_YOU_READY_TO_START,
		BET_TABLE_TRADING,
		TALON_SHOW, // if somebody decided to play
		FIRST_CARD_TALON_SHOW, // if raspasy
		SECOND_CARD_TALON_SHOW,
		TALON_GOES_TO_PLAYER,
		THROW_EXTRA_CARDS,
		PLAYER_CHOOSES_REAL_GAME_BET,
		DECIDE_TO_VIST_OR_PASS,
		DECIDE_OPENED_OR_CLOSED_GAME,
		SHOW_PASSER_CARDS
	}
	
    public PlayingTableView(Context context) {
    	super(context);
    	temp = 0;
    	gameHolder = null;
    	mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

    	ownCards = new PlayingCard[12];
    	prevCards =  new PlayingCard[12];
    	nextCards =  new PlayingCard[12];
    	talonCards =  new PlayingCard[2];

    	for (int i = 0; i < 12; i++)
    	{
    		ownCards[i] = new PlayingCard(this);
    		prevCards[i] = new PlayingCard(this);
    		nextCards[i] = new PlayingCard(this);

    	}
    	for (int i = 0; i < 2; i++) {
    		talonCards[i] = new PlayingCard(this);
    	}
    	cardMoveFromNext = new PlayingCard(this);
    	cardMoveFromPrev = new PlayingCard(this);
    	cardMoveFromOwn = new PlayingCard(this);
    	threeCards = new PlayingCard[3];
    	
    	movingCard = null;
    	movingCardIndex = -1;
    	talonShowTimer = -1; // no showing means
    	gameInfo = GameInfo.getInstance();
    	setBackgroundDrawable(getResources().getDrawable(R.drawable.table));
    	gameActivity = (GameActivity)context;

    	Bitmap card = BitmapFactory.decodeResource(getResources(), R.drawable.card1);
    	Bitmap clowd = BitmapFactory.decodeResource(getResources(), R.drawable.clowd_left);

    	// TODO hardcode
    	rightClowd = new TalkClowd(this, false, PrefApplication.screenWidth - clowd.getWidth() - 20, 5);
    	leftClowd = new TalkClowd(this, true, 20, 5);
    	ownClowd = new TalkClowd(this, true, (PrefApplication.screenWidth - clowd.getWidth()) / 2, PrefApplication.screenHeight - 10 - card.getHeight() - clowd.getHeight());

    	card.recycle();
    	clowd.recycle();

    	//setWillNotDraw(true);
    	gameLoopThread = new GameLoopThread(this);
    	holder = getHolder();
    	holder.addCallback(new SurfaceHolder.Callback() {

    		@Override
    		public void surfaceDestroyed(SurfaceHolder holder) {
    			boolean retry = true;
    			/*set the flag to false */
    			gameLoopThread.setRunning(false);
    			while (retry) {
    				try {
    					gameLoopThread.join();
    					retry = false;
    				} catch (InterruptedException e) {
    					// we will try it again and again...
    				}
    			}

    			for (int i = 0; i < 12; i++) {
    				ownCards[i].destroy();
    				nextCards[i].destroy();
    				prevCards[i].destroy();
    			}
    			for (int i = 0; i < 2; i++) {
    				talonCards[i].destroy();
    			}
    			rightClowd.destroy();
    			leftClowd.destroy();
    			ownClowd.destroy();
    		}

    		@Override
    		public void surfaceCreated(SurfaceHolder holder) {
    			gameLoopThread.setRunning(true);
    			gameLoopThread.start();   
    		}

    		@Override
    		public void surfaceChanged(SurfaceHolder holder, int format,
    				int width, int height) {

    		}
    	});
    	
    	myName = new NameView(this.getContext());
    	myName.init(GameInfo.getInstance().ownPlayer.name);
    	myName.setVisibility(VISIBLE);
    	int w = myName.getMeasuredWidth();
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	params.setMargins(10, PrefApplication.screenHeight - 10, 100, PrefApplication.screenHeight);
    	myName.setLayoutParams(params);
    	myName.bringToFront();
    	
    	nextName = new NameView(this.getContext());
    	nextName.init(GameInfo.getInstance().nextPlayer.name);
    	nextName.setVisibility(INVISIBLE);
    	
    	prevName = new NameView(this.getContext());
    	prevName.init(GameInfo.getInstance().prevPlayer.name);
    	prevName.setVisibility(INVISIBLE);
	}
    
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
    	if (gameHolder != null)
    		gameHolder.onTouch(this, ev);
        mScaleDetector.onTouchEvent(ev);
        
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            final float x = ev.getX();
            final float y = ev.getY();

            mLastTouchX = x;
            mLastTouchY = y;
            
            // define if some card was touched
            if (gameInfo.activePlayer == gameInfo.ownPlayer.number) {
	            switch (gameInfo.gameState) {
	            case 3:
	            	if (gameInfo.thrownCards.cardsNumber == 2) 
	            		break;
		            movingCardIndex = -1;
		            for (int i = 0; i < 12; i++) {
		            	PlayingCard card = ownCards[i];
		            	if (card.getValue() != -1) {
		            		if (card.containtsPoint(x, y))
		            			movingCardIndex = i;
		            	}
		            }
		            if (movingCardIndex != -1) {
		            	movingCard = ownCards[movingCardIndex];
		            	movingCard.saveCurrentPosition();
		            } else {
		            	movingCard = null;
		            }
		            break;
	            case 9: // real game is going on
	            	movingCardIndex = -1;
	            	movingCard = null;
	            	if (gameInfo.cardsOnTable == 3 || cardMoveFromOwn.getValue() != -1) // already made a move
	            		break;
	            	for (int i = 0; i < 12; i++) {
		            	PlayingCard card = ownCards[i];
		            	if (card.getValue() != -1) {
		            		if (card.containtsPoint(x, y))
		            			movingCardIndex = i;
		            	}
		            }
	            	if (movingCardIndex != -1) {
	            		int mySuit = gameInfo.getCardSuit(ownCards[movingCardIndex].getValue());
	            		// check if the move is correct
	            		if (gameInfo.currentSuit != -1 && mySuit != gameInfo.currentSuit) {
	            			if (gameInfo.ownPlayer.hasNoSuit == false) {
	            				movingCardIndex = -1;
	            				break;
	            			}
	            			
	            			if (mySuit != gameInfo.currentTrump && gameInfo.ownPlayer.hasNoTrumps == false) {
	            				movingCardIndex = -1;
	            				break;
	            			}
	            		}	
	            		movingCard = ownCards[movingCardIndex];
	            		movingCard.saveCurrentPosition();
	            		movingCard.setFlipRectAndBehaviour(PrefApplication.screenHeight, 0, PrefApplication.screenHeight - 2 * movingCard.height, PrefApplication.screenWidth, SIDE.FRONT, SIDE.FRONT);
	            	}
	            	break;
	            }
	            
            }
            mActivePointerId = ev.getPointerId(0);
            break;
        }
        
        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);

            if (gameInfo.activePlayer == gameInfo.ownPlayer.number) {
            	// Only move if the ScaleGestureDetector isn't processing a gesture.
            	if (!mScaleDetector.isInProgress()) {

            		if (movingCard != null) {
            			movingCard.translate(x - mLastTouchX, y - mLastTouchY);

            			switch (gameInfo.gameState) {
            			case 3:
            				break;
            			case 9:
            				break;
            			}
            		} else { // else maybe user wants to see score table

            		}
            	}
            	mLastTouchX = x;
            	mLastTouchY = y;
            }
            break;
        }

        case MotionEvent.ACTION_UP: {
            mActivePointerId = INVALID_POINTER_ID;
            switch (gameInfo.gameState) {
            case 3:
            	if (movingCard != null) {
            		if (movingCard.getY() + 3 * movingCard.height + 10 < getHeight())
            		{
            			int n = gameInfo.thrownCards.cardsNumber++;
            			gameInfo.thrownCards.cards[n] = movingCard.getValue();
            			PlayingCard temp = talonCards[n];
            			talonCards[n] =  movingCard;
            			ownCards[movingCardIndex] = temp;
            		    ownCards[movingCardIndex].setValue(-1);
            		    ownCards[movingCardIndex].setVisibility(false);
            		    gameInfo.ownPlayer.cardsNumber--;
            		    this.recountOwnCardsPositions();
            		    //postInvalidate();
            		    int dy = n * talonCards[0].height / 10;
            		    int dx = n * talonCards[0].width / 7;
            		    talonCards[n].setMovingToDestination((getWidth() - talonCards[0].width + dx) / 2, 10 + dy, 15);
            			
            			if (n == 1) {
            				// send info about thrown cards to server
            				gameActivity.sendThrownCardsToServer();
            				
            			}
            			
            		} else { // it goes back to user
            			movingCard.setMovingToSavedPos(15);
            		}
        			movingCard = null;
        			movingCardIndex = -1;
            	}
            	break;
            case 9:
            	if (movingCard != null) {
            		if (movingCard.getY() + 3 * movingCard.height + 10 < getHeight())
            		{
            			gameInfo.ownPlayer.lastCardMove = movingCard.getValue();
            			PlayingCard temp = cardMoveFromOwn;
            			cardMoveFromOwn = movingCard;
            			ownCards[movingCardIndex] = temp;
            		    ownCards[movingCardIndex].setValue(-1);
            		    ownCards[movingCardIndex].setVisibility(false);
            		    gameInfo.ownPlayer.cardsNumber--;
            		    movingCard = null;
            		    movingCardIndex = -1;
            		    this.recountOwnCardsPositions();
            		    //postInvalidate();
            		    int dy = 40; // hardcode
            		    int dx = PrefApplication.screenWidth;
            		    cardMoveFromOwn.setMovingToDestination((dx - cardMoveFromOwn.width) / 2, 10 + dy, 15);
            		    
            		    gameInfo.ownPlayer.moveIsDrawn = true;
            			threeCards[gameInfo.cardsOnTable] = cardMoveFromOwn;
            			
            			gameActivity.sendMyCardMoveToServer();           				           			
            		} else { // it goes back to user
            			movingCard.setMovingToSavedPos(15);
            		}
        			movingCard = null;
        			movingCardIndex = -1;
            	}
            	break;
            }
            break;
           
        }

        case MotionEvent.ACTION_CANCEL: {
            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = ev.getX(newPointerIndex);
                mLastTouchY = ev.getY(newPointerIndex);
                mActivePointerId = ev.getPointerId(newPointerIndex);
                
            }

        }
        break;
        }

        return true;
    }
    
    
    @Override  
    public void onDraw(Canvas canvas) {
        canvas.save();
        
        for (int i = 0; i < 12; i++)
        {
        	if (i != movingCardIndex)
        	{
	        	if (ownCards[i].getValue() != -1) 
	        		ownCards[i].draw(canvas);
        	}
        	if (prevCards[i].getValue() != -1) 
        		prevCards[i].draw(canvas);
        	if (nextCards[i].getValue() != -1) 
        		nextCards[i].draw(canvas);
        	
        }
        for (int i = 0; i < 2; i++)
        {
        	if (talonCards[i].getValue() != -1)
        		talonCards[i].draw(canvas);
        }
        
        if (movingCardIndex != -1) // to make it last drawn card
        	ownCards[movingCardIndex].draw(canvas);
        
        for (int i = 0; i < 3; i++) {
        	if (threeCards[i] != null)
        		threeCards[i].draw(canvas);
        }
        
        ownClowd.draw(canvas);
        leftClowd.draw(canvas);
        rightClowd.draw(canvas);
        canvas.restore();
    }
 
    public void clearThreeCards() {
    	for (int i = 0; i < 3; i++)
    		threeCards[i] = null;
    }
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            //invalidate();
            return true;
        }
    }
	
    private Bitmap decodeCard(int cardValue) {
    	if (cardValue > 0 && cardValue < 33) {
	    	String name = "card".concat(Integer.toString(cardValue));
	    	int resourceId = getResources().getIdentifier(name, "drawable", PrefApplication.getInstance().getPackageName());
			Bitmap cardBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
			return cardBitmap;
    	} else if (cardValue == 0) {
    		Bitmap cardBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.back);
			return cardBitmap;
    	} else {
    		Log.i("Error: ", "File with this card number doesn't exist.");
    	}
    	return null;
    }
    
	public void setNewCardsOnHand(int[] _cards) {
		if (ownCards == null)
			return;
		int n = gameInfo.ownPlayer.cardsNumber;
		for (int i = 0; i < n; i++) {
			ownCards[i].changeBitmap(decodeCard(_cards[i]), SIDE.FRONT);
			ownCards[i].setValue(_cards[i]);
			ownCards[i].setVisibility(true);
		}
		recountOwnCardsPositions();
	}
	
	public void leftCardsChanged(int[] _cards) {
		int n = gameInfo.nextPlayer.cardsNumber;
		if (gameInfo.nextPlayer.cardsAreVisible == false) {
			Bitmap cardBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.back);
			for (int i = 0; i < n; i++) {
				nextCards[i].changeBitmap(cardBitmap, SIDE.BACK);
				nextCards[i].setValue(0); // means that it's turned 
				nextCards[i].setVisibility(true);
			}
		} else {
			for (int i = 0; i < n; i++) {
				nextCards[i].changeBitmap(decodeCard(_cards[i]), SIDE.FRONT);
				nextCards[i].setValue(_cards[i]);
				nextCards[i].setVisibility(true);
			}
		}
		recountLeftCardsPositions();
	}
	
	public void updateCardFlips() {
		switch (gameInfo.gameState) {
		case 3:
			for (PlayingCard card : prevCards) 
				card.setFlipRectAndBehaviour(PrefApplication.screenHeight, PrefApplication.screenWidth - 1.5f * card.width, 0, PrefApplication.screenWidth, SIDE.BACK, SIDE.BACK);
			for (PlayingCard card : nextCards) 
				card.setFlipRectAndBehaviour(PrefApplication.screenHeight, 0, 0, 1.5f * card.width, SIDE.BACK, SIDE.BACK);
			for (PlayingCard card : ownCards) 
				card.setFlipRectAndBehaviour(PrefApplication.screenHeight, 0, 2 * card.height, PrefApplication.screenWidth, SIDE.FRONT, SIDE.BACK);
			break;
		case 10:
			for (PlayingCard card : prevCards) 
				card.setFlipRectAndBehaviour(PrefApplication.screenHeight, PrefApplication.screenWidth - 1.5f * card.width, 0, PrefApplication.screenWidth, SIDE.BACK, SIDE.FRONT);
			for (PlayingCard card : nextCards) 
				card.setFlipRectAndBehaviour(PrefApplication.screenHeight, 0, 0, 1.5f * card.width, SIDE.BACK, SIDE.FRONT);
			for (PlayingCard card : ownCards) 
				card.setFlipRectAndBehaviour(PrefApplication.screenHeight, 0, 2 * card.height, PrefApplication.screenWidth, SIDE.FRONT, SIDE.FRONT);

			break;
		}
	}
	
	public void rightCardsChanged(int[] _cards) {
		int n = gameInfo.prevPlayer.cardsNumber;
		if (gameInfo.prevPlayer.cardsAreVisible == false) {
			Bitmap cardBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.back);
			for (int i = 0; i < n; i++) {
				prevCards[i].changeBitmap(cardBitmap, SIDE.BACK);
				prevCards[i].setValue(0); // means that it's turned 
				prevCards[i].setVisibility(true);
			}
		} else {
			for (int i = 0; i < n; i++) {
				prevCards[i].changeBitmap(decodeCard(_cards[i]), SIDE.FRONT);
				prevCards[i].setValue(_cards[i]);
				prevCards[i].setVisibility(true);
			}
		}
		recountRightCardsPositions();
	}
	
	public void talonChanged(int[] _cards) {
		int n = gameInfo.talon.cardsNumber;
		for (int i = 0; i < n; i++) {
			if (_cards == null) {
				Bitmap cardBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.back);
				talonCards[i].changeBitmap(cardBitmap, SIDE.BACK);
				talonCards[i].setValue(0);
			} else {
				talonCards[i].setValue(_cards[i]);
				talonCards[i].changeBitmap(decodeCard(_cards[i]), SIDE.FRONT);
			}
			talonCards[i].setVisibility(true);
		}
		recountTalonCardsPositions();
	}
	
	public void showCardsOnTable(int[] myCards, int[] leftCards, int[] rightCards) {
		setNewCardsOnHand(myCards);
		leftCardsChanged(leftCards);
		rightCardsChanged(rightCards);
		talonChanged(null);
	}
	
	private void recountOwnCardsPositions() {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.card18);
		int dx = 0;
		int y = getHeight() - bmp.getHeight() - 10;
		if (gameInfo.ownPlayer.cardsNumber > 1)
		{
			dx = (getWidth()  - 20 - bmp.getWidth())/ (gameInfo.ownPlayer.cardsNumber - 1);
			dx = Math.min(dx, 2 * bmp.getWidth() / 3);
		}
		int x = 10; // 10 is start coord of first card
		for (int i = 0; i < 12; i++) {
			if (ownCards[i].getValue() != -1) {
				ownCards[i].setPosition(x, y);
				x += dx;
			}
		}
		//if (redraw)
			//postInvalidate();
	}
	
	private void recountLeftCardsPositions() {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.card18);
		int x = 10;
		int dy = 0;
		int n = gameInfo.nextPlayer.cardsNumber;
		if (n > 1) {
			dy = (getHeight()  - 30 - 2 * bmp.getHeight() - 10)/ (n - 1);
			dy = Math.min(dy, 2 * bmp.getHeight() / 3);
		}
		int y = 10;
		for (int i = 0; i < n; i++) {
			nextCards[i].setPosition(x, y);
			y += dy;
		}
	}
	
	private void recountRightCardsPositions() {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.card18);
		int x = getWidth() - 10 - bmp.getWidth();
		int dy = 0;
		int n = gameInfo.prevPlayer.cardsNumber;
		if (n > 1) {
			dy = (getHeight()  - 30 - 2 * bmp.getHeight() - 10)/ (n - 1);
			dy = Math.min(dy, 2 * bmp.getHeight() / 3);
		}
		int y = 10;
		for (int i = 0; i < n; i++) {
			prevCards[i].setPosition(x, y);
			y += dy;
		}
	}
	
	private void recountTalonCardsPositions() {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.card18);
		int dy = 0;
		int dx = 0;
		int n = gameInfo.talon.cardsNumber;
		if (n > 1) {
			dy = bmp.getHeight() / 10;
			dx = bmp.getWidth() / 8;
		}
		int x = (getWidth() - dx - bmp.getWidth()) / 2;
		int y = 30;
		for (int i = 0; i < n; i++) {
			talonCards[i].setPosition(x, y);
			y += dy;
			x += dx;
		}
	}
	
	private int findUnusedId() {
		int id = 1;
		while (findViewById(++id) != null);
		return id;
	}

	
	public void addTalonCardsToPlayer() {
		PlayingCard[] ref;
		PlayingCard[] movingCards = new PlayingCard[2];
		int flag = 0;
		if (gameInfo.activePlayer == gameInfo.ownPlayer.number) {
			flag = 1;
			ref = ownCards;
			gameInfo.ownPlayer.cardsNumber = 12;
		} else if (gameInfo.activePlayer == gameInfo.nextPlayer.number) {
			ref = nextCards;
			gameInfo.nextPlayer.cardsNumber = 12;
			flag = 2;
		} else {
			ref = prevCards;
			gameInfo.prevPlayer.cardsNumber = 12;
			flag = 3;
		}

		for (int i = 0; i < 2; i++) {
			PlayingCard temp = ref[i + 10];
			movingCards[i] = ref[i + 10] = talonCards[i];
			talonCards[i] = temp;
			talonCards[i].setValue(-1);
			talonCards[i].setVisibility(false);
		}
		
		// now animation settings
		for (int i = 10; i < 12; i++) {
			ref[i].saveCurrentPosition();
		}
		
		SIDE finishSide = SIDE.BACK;
		switch (flag) {
		case 1:
			finishSide = SIDE.FRONT;
			for (int i = 10; i < 12; i++) {
				int j = 0;
				while (ownCards[j].getValue() < ownCards[i].getValue()) {
					j++;
				}
				if (j < i) {
					for (int k = i; k > j; k--) {
						PlayingCard temp = ownCards[k];
						ownCards[k] = ownCards[k - 1];
						ownCards[k - 1] = temp;
					}
				}
			}
			recountOwnCardsPositions();
			break;
		case 2:
			recountLeftCardsPositions();
			break;
		case 3:
			recountRightCardsPositions();
			break;
		}
		
		for (int i = 0; i < 2; i++) {
			int tempX = movingCards[i].getX();
			int tempY = movingCards[i].getY();
			movingCards[i].restorePosition();
			
			movingCards[i].setFlipRectAndBehaviour(PrefApplication.screenHeight, 1.5f * movingCards[i].width, 0, PrefApplication.screenWidth - 1.5f * movingCards[i].width, finishSide, SIDE.FRONT);
			movingCards[i].setMovingToDestination(tempX, tempY, 15);
		}
		updateCardFlips();
	}

	public void setDrawState(DrawState state) {
		drawState = state;
	}
	
	public void drawThrownCards() {
		if (gameInfo.activePlayer == gameInfo.prevPlayer.number) {
			gameInfo.prevPlayer.cardsNumber = 10;
			for (int i = 0; i < 2; i++) {
				PlayingCard temp = talonCards[i];
				talonCards[i] = prevCards[10 + i];
				prevCards[i + 10] = temp;
				prevCards[i + 10].setValue(-1);
				prevCards[i + 10].setVisibility(false);
				int dy = i * talonCards[0].height / 10;
    		    int dx = i * talonCards[0].width / 7;
				talonCards[i].setMovingToDestination((getWidth() - talonCards[0].width + dx) / 2, 10 + dy, 15);
				recountRightCardsPositions();
			}
		} else {
			gameInfo.nextPlayer.cardsNumber = 10;
			for (int i = 0; i < 2; i++) {
				PlayingCard temp = talonCards[i];
				talonCards[i] = nextCards[10 + i];
				nextCards[i + 10] = temp;
				prevCards[i + 10].setValue(-1);
				prevCards[i + 10].setVisibility(false);
				int dy = i * talonCards[0].height / 10;
    		    int dx = i * talonCards[0].width / 7;
				talonCards[i].setMovingToDestination((getWidth() - talonCards[0].width + dx) / 2, 10 + dy, 15);
				recountLeftCardsPositions();
			}
		}
	}
	
	public void showMyClowd() {
		ownClowd.setVisible(true);
		//postInvalidate();
	}
	
	public void showLeftClowd() {
		leftClowd.setVisible(true);
		//postInvalidate();
	}

	public void showRightClowd() {
		rightClowd.setVisible(true);
		//postInvalidate();
	}
	
	public void showMyClowdBet() {
		if (gameInfo.ownPlayer.myNewBet != -1) {
			ownClowd.setBet(gameInfo.ownPlayer.myNewBet);
			ownClowd.setVisible(true);
		} else {
			ownClowd.setVisible(false);
		}
		//postInvalidate();
	}
	
	public void showLeftClowdBet() {
		if (gameInfo.nextPlayer.myNewBet != -1) {
			leftClowd.setBet(gameInfo.nextPlayer.myNewBet);
			leftClowd.setVisible(true);
		} else
			leftClowd.setVisible(false);
		//postInvalidate();
	}

	public void showRightClowdBet() {
		if (gameInfo.prevPlayer.myNewBet != -1) {
			rightClowd.setBet(gameInfo.prevPlayer.myNewBet);
			rightClowd.setVisible(true);
		} else
			rightClowd.setVisible(false);
		//postInvalidate();
	}
	
	public void hideLeftClowd() {
		if (leftClowd.getVisible() == true) {
			leftClowd.setVisible(false);
			//postInvalidate();
		}
	}
	
	public void hideRightClowd() {
		if (rightClowd.getVisible() == true) {
			rightClowd.setVisible(false);
		//	postInvalidate();
		}
	}
	
	public void hideOwnClowd() {
		if (ownClowd.getVisible() == true) {
			ownClowd.setVisible(false);
			//postInvalidate();
		}
	}
	
	public void updateLastCardMove() {
		if (gameInfo.nextPlayer.lastCardMove != -1 && gameInfo.nextPlayer.moveIsDrawn == false) {
			gameInfo.nextPlayer.moveIsDrawn = true;
			gameInfo.nextPlayer.cardsNumber--;
			int i = 0;
			while (i < gameInfo.nextPlayer.cardsNumber && nextCards[i].getValue() != -1)
				i++;
			
			PlayingCard temp = cardMoveFromNext;
			cardMoveFromNext = nextCards[i];
			nextCards[i] = temp;
			temp.setValue(-1);
			temp.setVisibility(false);
			
			cardMoveFromNext.setValue(gameInfo.nextPlayer.lastCardMove);
			cardMoveFromNext.setFlipRectAndBehaviour(PrefApplication.screenHeight, 0, 0, cardMoveFromNext.width, SIDE.BACK, SIDE.FRONT);
			int lastX = PrefApplication.screenWidth / 2 - 4 * cardMoveFromNext.width / 6;
			int lastY = 20; // TODO hardcode
			cardMoveFromNext.setMovingToDestination(lastX, lastY, 10);
			
			for (i = 0; i < 3; i++) {
				if (threeCards[i] == null) {
					threeCards[i] = cardMoveFromNext;
					break;
				}
			}
		} else if (gameInfo.nextPlayer.lastCardMove == -1) {
			gameInfo.nextPlayer.moveIsDrawn = false;
			cardMoveFromNext.setValue(-1);
			cardMoveFromNext.setVisibility(false);
		}
		
		if (gameInfo.prevPlayer.lastCardMove != -1 && gameInfo.prevPlayer.moveIsDrawn == false) {
			gameInfo.prevPlayer.moveIsDrawn = true;
			gameInfo.prevPlayer.cardsNumber--;
			int i = 0;
			while (i < gameInfo.prevPlayer.cardsNumber && prevCards[i].getValue() != -1)
				i++;
			
			PlayingCard temp = cardMoveFromPrev;
			cardMoveFromPrev = prevCards[i];
			prevCards[i] = temp;
			temp.setValue(-1);
			temp.setVisibility(false);
			
			cardMoveFromPrev.setValue(gameInfo.prevPlayer.lastCardMove);
			cardMoveFromNext.setFlipRectAndBehaviour(PrefApplication.screenHeight, 0, 0, cardMoveFromNext.width, SIDE.BACK, SIDE.FRONT);
			int lastX = PrefApplication.screenWidth / 2  - 2 * cardMoveFromPrev.width / 6;
			int lastY = 20; // TODO hardcode
			cardMoveFromPrev.setMovingToDestination(lastX, lastY, 10);
			
			for (i = 0; i < 3; i++) {
				if (threeCards[i] == null) {
					threeCards[i] = cardMoveFromPrev;
					break;
				}
			}
		} else if (gameInfo.prevPlayer.lastCardMove == -1) {
			gameInfo.prevPlayer.moveIsDrawn = false;
			cardMoveFromPrev.setValue(-1);
			cardMoveFromPrev.setVisibility(false);
		}
		
		if (gameInfo.ownPlayer.lastCardMove == -1) {
			cardMoveFromOwn.setValue(-1);
			cardMoveFromOwn.setVisibility(false);
			gameInfo.ownPlayer.moveIsDrawn = false;
		}
	}
	
	public void updateRoomInfo() {
		nextName.setText(gameInfo.nextPlayer.name);
		if (!gameInfo.nextPlayer.name.isEmpty())
			nextName.setVisibility(VISIBLE);
		prevName.setText(gameInfo.prevPlayer.name);
		if (!gameInfo.prevPlayer.name.isEmpty())
			prevName.setVisibility(VISIBLE);
	}
	
	public void hideThrownCards() {
		for (int i = 0; i < 2; i++) {
			talonCards[i].setVisibility(false);
			talonCards[i].setValue(-1);
		}
	}
	
	public void showCurrentRolesClowds() {
		switch (gameInfo.prevPlayer.myRole) {
		case 1:
			rightClowd.setText("whist");
			showRightClowd();
			break;
		case 2:
			rightClowd.setBet(gameInfo.currentCardBet);
			showRightClowd();
			break;
		}

		switch (gameInfo.nextPlayer.myRole) {
		case 1:
			leftClowd.setText("whist");
			showLeftClowd();
			break;
		case 2:
			leftClowd.setBet(gameInfo.currentCardBet);
			showLeftClowd();
			break;
		}
	}
	
	public void CheckIfCanThrowEverything() {
		gameInfo.ownPlayer.hasNoSuit = true;
		gameInfo.ownPlayer.hasNoTrumps = true;
		for (int i = 0; i < 12; i++) {
			int suit = gameInfo.getCardSuit(ownCards[i].getValue());
			if (suit == gameInfo.currentSuit)
				gameInfo.ownPlayer.hasNoSuit = false;
			if (suit == gameInfo.currentTrump)
				gameInfo.ownPlayer.hasNoTrumps = false;
		}
	}
	
	public void initNewThreeCards() {
		for (int i = 0; i < 3; i++)
			threeCards[i] = null;
	}
	
}

