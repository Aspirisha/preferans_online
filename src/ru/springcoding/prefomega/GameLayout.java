package ru.springcoding.prefomega;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class GameLayout extends LinearLayout implements OnTouchListener {
	ViewFlipper flipper;
	GameView gameView;
    private ScoreView scoreView;
    float fromPosition;
    final float MOVE_LENGTH = PrefApplication.screenWidth * 2 / 3;
    
    public enum REDRAW_TYPE {
    	DRAW_THROWN_CARDS,
    	SHOW_LEFT_CLOWD,
    	SHOW_RIGHT_CLOWD,
    	SHOW_OWN_CLOWD,
    	HIDE_LEFT_CLOWD,
    	HIDE_RIGHT_CLOWD,
    	HIDE_OWN_CLOWD,
    }
    
    
	public GameLayout(Context context) {
		super(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		flipper = new ViewFlipper(context);
		flipper.setLayoutParams(params);
		addView(flipper);
		
		
		gameView = new GameView(context);
        scoreView = new ScoreView(context);
		flipper.addView(gameView, params);
		flipper.addView(scoreView);
		
		gameView.setGameHolder(this);
		setOnTouchListener(this);
	}
	
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    PrefApplication.setVisibleAreaSize(getMeasuredHeight(), getMeasuredWidth());
	    scoreView.recountCoordinates();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
	    switch (event.getAction())
	    {
	    case MotionEvent.ACTION_DOWN: // ѕользователь нажал на экран, т.е. начало движени€ 
	        // fromPosition - координата по оси X начала выполнени€ операции
	        fromPosition = event.getX();
	        break;
	    case MotionEvent.ACTION_MOVE:
	        float toPosition = event.getX();
	        // MOVE_LENGTH - рассто€ние по оси X, после которого можно переходить на след. экран
	        // ¬ моем тестовом примере MOVE_LENGTH = 150
	        if ((fromPosition - MOVE_LENGTH) > toPosition)
	        {
	        	fromPosition = toPosition;
	            flipper.setInAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_next_in));
	            flipper.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_next_out));
	            flipper.showNext();
	        }
	        else if ((fromPosition + MOVE_LENGTH) < toPosition)
	        {
	        	fromPosition = toPosition;
	            flipper.setInAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_prev_in));
	            flipper.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_prev_out));
	            flipper.showPrevious();
	        }
	    }
	    return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float MOVE_LENGTH = 150;
	    switch (event.getAction())
	    {
	    case MotionEvent.ACTION_DOWN: // ѕользователь нажал на экран, т.е. начало движени€ 
	        // fromPosition - координата по оси X начала выполнени€ операции
	        fromPosition = event.getX();
	        break;
	    case MotionEvent.ACTION_MOVE:
	        float toPosition = event.getX();
	        // MOVE_LENGTH - рассто€ние по оси X, после которого можно переходить на след. экран
	        // ¬ моем тестовом примере MOVE_LENGTH = 150
	        if ((fromPosition - MOVE_LENGTH) > toPosition)
	        {
	        	fromPosition = toPosition;
	            flipper.setInAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_next_in));
	            flipper.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_next_out));
	            flipper.showNext();
	        }
	        else if ((fromPosition + MOVE_LENGTH) < toPosition)
	        {
	        	fromPosition = toPosition;
	            flipper.setInAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_prev_in));
	            flipper.setOutAnimation(AnimationUtils.loadAnimation(this.getContext(),R.anim.go_prev_out));
	            flipper.showPrevious();
	        }
	    }
		return false;
	}

	public void updateScores() {
		scoreView.updateScoreTable();
	}
}