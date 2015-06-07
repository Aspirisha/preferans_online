package ru.springcoding.prefomega;

import ru.springcoding.prefomega.ChoiceCellView.CHOICE;
import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.FrameLayout.LayoutParams;

public class ChoiceTable extends LinearLayout {
	private TableLayout choiceTableSmall;
    private TableLayout choiceTableBig;
    private ChoiceCellView leftChoice;
    private ChoiceCellView rightChoice;
    private CHOICE_TABLE_STATE choiceTableState;
    
    enum CHOICE_TABLE_STATE {
    	EMPTY,
    	WHISTING,
    	OPEN_CLOSE, // type of whisting if one is passing
    	WHO_TESTS_MISERE
    }
    
    public ChoiceTable(Context context) {
		super(context);
		choiceTableSmall = new TableLayout(context);
        choiceTableBig = new TableLayout(context);
        choiceTableState = CHOICE_TABLE_STATE.EMPTY;
        
    	TableRow row = new TableRow(context);
		int alphaValue = 200;
		//this.setPadding(10, 10, 10+PrefApplication.screenWidth, 10+PrefApplication.screenHeight);
		ChoiceCellView v = new ChoiceCellView(context);
		v.init(CHOICE.LEFT_VARIANT, R.drawable.choiceback_selected, R.drawable.choiceback_unselected, "whist");
		v.getBackground().setAlpha(alphaValue);
		row.addView(v);
		leftChoice = v;
		leftChoice.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		
		v = new ChoiceCellView(context);
		v.init(CHOICE.RIGHT_VARIANT, R.drawable.choiceback_selected, R.drawable.choiceback_unselected, "pass");
		v.getBackground().setAlpha(alphaValue);
		row.addView(v);
		rightChoice = v;
		rightChoice.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		choiceTableSmall.addView(row);
		choiceTableSmall.layout(30, PrefApplication.screenHeight / 2 - v.getBackground().getIntrinsicHeight(), 30 + 2 * v.getBackground().getIntrinsicWidth(), PrefApplication.screenHeight / 2);
		
		row = new TableRow(context);
		v = new ChoiceCellView(context);
		v.init(CHOICE.OK, R.drawable.acceptback_selected, R.drawable.acceptback_unselected, "accept");
		v.getBackground().setAlpha(alphaValue);
		row.addView(v);
		v.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		choiceTableBig.addView(row);
        choiceTableBig.layout(30, PrefApplication.screenHeight / 2, 30 + v.getBackground().getIntrinsicWidth(), PrefApplication.screenHeight / 2 + v.getBackground().getIntrinsicHeight());
        		
		addView(choiceTableSmall);
		addView(choiceTableBig);
	}
    
    int getRawWidth() {
    	return choiceTableSmall.getWidth();
    }
    
	public void hideChoiceTable() {
		choiceTableSmall.setVisibility(INVISIBLE);
		choiceTableBig.setVisibility(INVISIBLE);
	}
	
	
	public void showWhistingTable() {
		if (choiceTableState != CHOICE_TABLE_STATE.WHISTING) {
			leftChoice.setText("whist");
			rightChoice.setText("pass");
		}
		choiceTableSmall.bringToFront();
		choiceTableBig.bringToFront();
		choiceTableSmall.setVisibility(VISIBLE);
		choiceTableBig.setVisibility(VISIBLE);
	}
	
	public void showVisOrInvisTable() {
		if (choiceTableState != CHOICE_TABLE_STATE.OPEN_CLOSE) {
			leftChoice.setText("open");
			rightChoice.setText("close");
		}
		choiceTableSmall.bringToFront();
		choiceTableBig.bringToFront();
		choiceTableSmall.setVisibility(VISIBLE);
		choiceTableBig.setVisibility(VISIBLE);
	}
	
	public void showWhoTestsTable() {
		if (choiceTableState != CHOICE_TABLE_STATE.WHO_TESTS_MISERE) {
			leftChoice.setText("me");
			rightChoice.setText("companion");
		}
		choiceTableSmall.bringToFront();
		choiceTableBig.bringToFront();
		choiceTableSmall.setVisibility(VISIBLE);
		choiceTableBig.setVisibility(VISIBLE);
	}

}
