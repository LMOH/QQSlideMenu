package com.lmoh.qqslidemenu.test;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SlideMenu extends FrameLayout {

	private View menuView;
	private View mainView;
	private FloatEvaluator floatEvaluator;// float的计算器
	private IntEvaluator intEvaluator;// int的计算器

	public SlideMenu(Context context) {
		super(context);
		init();
	}

	public SlideMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		viewDragHelper = ViewDragHelper.create(this, callback);
		floatEvaluator = new FloatEvaluator();
		intEvaluator = new IntEvaluator();
	}
	
	//定义状态常量
		enum DragState{
			Open,Close;
		}
		public DragState currentState = DragState.Close;//当前SlideMenu的状态默认是关闭的

	// 使用ViewDragHelper需重写下两个方法，传递消费事件
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return viewDragHelper.shouldInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		viewDragHelper.processTouchEvent(event);
		return true;
	}

	ViewDragHelper.Callback callback = new Callback() {

		// 给两个子view设置触摸
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == mainView || child == menuView;
		}

		/**
		 * 获取view水平方向的拖拽范围,但是目前不能限制边界,返回的值目前用在手指抬起的时候view缓慢移动的动画世界的计算上面; 最好不要返回0
		 */
		public int getViewHorizontalDragRange(View child) {
			return (int) dragRange;
		}

		/**
		 * 控制child在水平方向的移动 left:
		 * 表示ViewDragHelper认为你想让当前child的left改变的值,left=chile.getLeft()+dx dx:
		 * 本次child水平方向移动的距离 return: 表示你真正想让child的left变成的值
		 */
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (child == mainView) {
				if (left < 0)
					left = 0;// 限制mainView的左边
				if (left > dragRange)
					left = (int) dragRange;// 限制mainView的右边
			}
			return left;
		}

		/**
		 * 伴随：拖动menuView，自己不动mainView动
		 */
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			if (changedView == menuView) {
				// 固定menuView
				menuView.layout(0, 0, menuView.getMeasuredWidth(), menuView.getMeasuredHeight());
				// 让mainView动,限制边界
				int newLeft = mainView.getLeft() + dx;
				if (newLeft < 0)
					newLeft = 0;// 限制mainView的左边
				if (newLeft > dragRange)
					newLeft = (int) dragRange;// 限制mainView的右边
				mainView.layout(newLeft, mainView.getTop() + dy, newLeft + mainView.getMeasuredWidth(),
						mainView.getBottom() + dy);
			}
			// 1.计算滑动的百分比
			float fraction = mainView.getLeft() / dragRange;
			// System.out.println("fraction="+fraction);
			// 2.执行伴随动画
			executeAnim(fraction);
			//3.更改状态，回调listener的方法
			if(fraction==0 && currentState!=DragState.Close){
				//更改状态为关闭，并回调关闭的方法
				currentState = DragState.Close;
				if(listener!=null){listener.onClose();}
			}else if (fraction==1f && currentState!=DragState.Open) {
				//更改状态为打开，并回调打开的方法
				currentState = DragState.Open;
				if(listener!=null){listener.onOpen();}
			}
			//将drag的fraction暴漏给外界
			if(listener!=null){
				listener.onDraging(fraction);
			}
		}

		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// 判断抬起时mainView的所处访问执行指定动画(平滑滚动)
			// 记得重写computeScroll()
			if (mainView.getLeft() < dragRange / 2) {
				// 向左
				close();
			} else {
				// 向右
				 open();
			}

			//处理用户的稍微滑动
			if(xvel>200 && currentState!=DragState.Open){
				open();
			}else if (xvel<-200 && currentState!=DragState.Close) {
				close();
			}
		}
	};
	
	/**
	 * 关闭菜单
	 */
	public void close() {
		System.out.println("关闭menu");
		viewDragHelper.smoothSlideViewTo(mainView,0,mainView.getTop());
		ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
	}
	/**
	 * 打开菜单
	 */
	public void open() {
		System.out.println("打开menu");
		viewDragHelper.smoothSlideViewTo(mainView,(int) dragRange,mainView.getTop());
		ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
	}
	
	/**
	 * 获取当前的状态
	 * @return
	 */
	public DragState getCurrentState(){
		return currentState;
	}

	/**
	 * 配合缓慢动画
	 */
	public void computeScroll() {
		if (viewDragHelper.continueSettling(true)) {
			// 刷新整个布局
			ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
		}
	};

	protected void executeAnim(float fraction) {
		// fraction:0-1
		// 缩小mainView
		// float scaleValue = 0.8f+0.2f*(1-fraction);//1-0.8f
		/*
		 * ViewHelper.setScaleX(mainView, scaleValue);
		 * ViewHelper.setScaleY(mainView, scaleValue);
		 */
		// 系统自带计算器
		ViewHelper.setScaleX(mainView, floatEvaluator.evaluate(fraction, 1f, 0.9f));
		ViewHelper.setScaleY(mainView, floatEvaluator.evaluate(fraction, 1f, 0.9f));
		// 移动menuView
		ViewHelper.setTranslationX(menuView, intEvaluator.evaluate(fraction, -menuView.getMeasuredWidth() / 2, 0));
		// 放大menuView
		ViewHelper.setScaleX(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1f));
		ViewHelper.setScaleY(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1f));
		// 改变menuView的透明度
		ViewHelper.setAlpha(menuView, floatEvaluator.evaluate(fraction, 0.3f, 1f));
		// 给SlideMenu的背景添加黑色的遮罩效果
		getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(fraction, Color.BLACK, Color.TRANSPARENT),
				Mode.SRC_OVER);
	}

	private ViewDragHelper viewDragHelper;
	private int width;
	private float dragRange;

	// 得到两个子view,异常处理：只能有两个子view
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (getChildCount() != 2) {
			throw new IllegalArgumentException("SlideMenu only have 2 children!");
		}
		menuView = getChildAt(0);
		mainView = getChildAt(1);
	}

	/**
	 * 该方法在onMeasure执行完之后执行，那么可以在该方法中初始化自己和子View的宽高
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = getMeasuredWidth();
		//
		dragRange = width * 0.6f;
	}

	private OnDragStateChangeListener listener;
	public void setOnDragStateChangeListener(OnDragStateChangeListener listener){
		this.listener = listener;
	}
	public interface OnDragStateChangeListener{
		/**
		 * 打开的回调
		 */
		void onOpen();
		/**
		 * 关闭的回调
		 */
		void onClose();
		/**
		 * 正在拖拽中的回调
		 */
		void onDraging(float fraction);
	}
}
