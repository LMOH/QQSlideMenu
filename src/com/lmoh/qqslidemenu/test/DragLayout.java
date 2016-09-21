package com.lmoh.qqslidemenu.test;

import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 自定义ViewGroup 1.重写onFinishInflate()，初始化子View的引用 2.重写onMeasure()，测量子View:
 * 指定测量规则，测量子View 3.重写onLayout()，指定子View摆放位置
 * 
 * @author PC-LMOH 1.实现
 */
public class DragLayout extends FrameLayout {

	private View redView;
	private View greenView;
	

	private ViewDragHelper viewDragHelper;

	public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public DragLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		/**
		 * 参1：父控件 参2：灵敏度 参3：回调
		 **/
		viewDragHelper = ViewDragHelper.create(this, 1.0f, callBack);
	}

	/**使用ViewDragHelper，需重写下面两个方法
	 * */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 让ViewDragHelper 帮我们判断是否应该拦截
		boolean shouldInterceptTouchEvent = viewDragHelper.shouldInterceptTouchEvent(ev);
		return shouldInterceptTouchEvent;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 将触摸事件传递给ViewDragHelper,解析，监视
		viewDragHelper.processTouchEvent(event);
		return true; // 自己消费事件
	}

	// 匿名内部类
	private ViewDragHelper.Callback callBack = new Callback() {
		// 回调告知调用者触摸的相关数据

		/**
		 * 回调：用于判断是否捕捉当前child 的触摸事件 child:当前触摸的子View return:true:捕获并解析，false：不处理
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			// 使某些子view能被捕获
			if (child == greenView || child == redView) {
				return true;
			}
			return false;
		}

		/**
		 * 回调：当view被开始捕获和解析的回调 capturedChild:当前被捕获的子view activePointerId:
		 */
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			Log.d("tag", "onViewCaptured");
			super.onViewCaptured(capturedChild, activePointerId);
		}

		/**
		 * 回调：获取view水平方向的拖拽范围,但是目前不能限制边界，返回的值目前用在手指抬起时view缓慢移动动画的时间计算上面 最后不要返回0
		 */
		@Override
		public int getViewHorizontalDragRange(View child) {
			return getMeasuredWidth() - child.getMeasuredWidth();
		}

		/**
		 * 回调：控制child在水平方向的移动 left ： 表示ViewDragHelper认为你想让当前的child改变的值，left =
		 * child.getleft() + dx dx : 本次child移动的水平距离 return ： 表示你想让child的left
		 * 变成的值，即返回 left - dx 则不能移动
		 */
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (left < 0) { // 限制左边界
				left = 0;
			} else if (left > getMeasuredWidth() - child.getMeasuredWidth()) { // 限制右边界
				left = getMeasuredWidth() - child.getMeasuredWidth();
			}
			return left;
		}

		@Override
		public int getViewVerticalDragRange(View child) {
			return getMeasuredHeight() - child.getMeasuredHeight();
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			// 限制上下
			if (top < 0) {
				top = 0;
			} else if (top > getMeasuredHeight() - child.getMeasuredHeight()) {
				top = getMeasuredHeight() - child.getMeasuredHeight();
			}
			return top;
		}

		/**
		 * 当child的位置改变的时候执行,一般用来做其他子View的伴随移动 changedView：位置改变的child
		 * left：child当前最新的left top: child当前最新的top dx: 本次水平移动的距离 dy: 本次垂直移动的距离
		 */
		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			if (changedView == redView) {
				greenView.layout(greenView.getLeft() + dx, greenView.getTop() + dy, greenView.getRight() + dx,
						greenView.getBottom() + dy);
			} else if (changedView == greenView) {
				redView.layout(redView.getLeft() + dx, redView.getTop() + dy, redView.getRight() + dx,
						redView.getBottom() + dy);
			}

			// 1.计算view移动的百分比
			float fraction = changedView.getLeft() * 1f / (getMeasuredWidth() - changedView.getMeasuredWidth());
			Log.e("tag", "fraction:" + fraction);
			// 2.执行一系列的伴随动画
			executeAnim(fraction);
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			// 获取左中间值，判断控件所处左还是右
			int centerLeft = getMeasuredWidth() / 2 - releasedChild.getMeasuredWidth() / 2;
			int centerRight = getMeasuredWidth() / 2 + releasedChild.getMeasuredWidth() / 2;
			if (releasedChild.getLeft() < centerLeft) {
				// 在左半边，向左缓慢移动
				viewDragHelper.smoothSlideViewTo(releasedChild, 0, releasedChild.getTop());
				// 刷新整个布局
				ViewCompat.postInvalidateOnAnimation(DragLayout.this);
			} else {
				// 在右半边，向左缓慢移动
				viewDragHelper.smoothSlideViewTo(releasedChild, getMeasuredWidth() - releasedChild.getMeasuredWidth(),
						releasedChild.getTop());
				// 刷新整个布局
				ViewCompat.postInvalidateOnAnimation(DragLayout.this);
			}
		}

	};

	/**
	 * 配合缓慢动画
	 */
	public void computeScroll() {
		if (viewDragHelper.continueSettling(true)) {
			// 刷新整个布局
			ViewCompat.postInvalidateOnAnimation(DragLayout.this);
		}
	};

	/**
	 * 伴随动画，用的是nineoldandroids-2.4.0.jar包里的ViewHelper的动画效果
	 * 
	 * @param fraction
	 */
	protected void executeAnim(float fraction) {
		// fraction: 0 - 1
		// 缩放
		// ViewHelper.setScaleX(redView, 1+0.5f*fraction);
		// ViewHelper.setScaleY(redView, 1+0.5f*fraction);
		// 旋转
		// ViewHelper.setRotation(redView,360*fraction);//围绕z轴转
		ViewHelper.setRotationX(redView, 360 * fraction);// 围绕x轴转
		// ViewHelper.setRotationY(redView,360*fraction);//围绕y轴转
		ViewHelper.setRotationX(greenView, 360 * fraction);// 围绕z轴转
		// 平移
		// ViewHelper.setTranslationX(redView,80*fraction);
		// 透明
		// ViewHelper.setAlpha(redView, 1-fraction);

		// 设置过度颜色的渐变
		redView.setBackgroundColor((Integer) ColorUtil.evaluateColor(fraction, Color.RED, Color.BLUE));
		greenView.setBackgroundColor((Integer) ColorUtil.evaluateColor(fraction, Color.GREEN, Color.GRAY));
		setBackgroundColor((Integer) ColorUtil.evaluateColor(fraction, Color.WHITE, Color.YELLOW));
	}

	/*
	 * @Override protected void onMeasure(int widthMeasureSpec, int
	 * heightMeasureSpec) { super.onMeasure(widthMeasureSpec,
	 * heightMeasureSpec); //要测量我自己的子View //1.测量规则,1.1从dimen获取dp值
	 * 1.2从控件redView.getLayoutParams().width //int size =
	 * getResources().getDimension(R.dimen.textview_witdh);//100dp int
	 * measureSpec =
	 * MeasureSpec.makeMeasureSpec(redView.getLayoutParams().width,
	 * MeasureSpec.EXACTLY); //确定模式 redView.measure(measureSpec, measureSpec);
	 * greenView.measure(measureSpec, measureSpec);
	 * //父类测量子类（若没有特殊的测量需求，可用下方法替代子view自己测量，更简单） //measureChild(redView,
	 * widthMeasureSpec, heightMeasureSpec); }
	 */
	/**
	 * 重写帧布局摆放方法，自己按需求摆放，否则子控件会随帧布局原生摆放（堆在左上角）
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// 1.确定左，上两处
		int left = getPaddingLeft() + getWidth() / 2 - redView.getMeasuredWidth() / 2;
		int top = getPaddingTop();
		redView.layout(left, top, redView.getMeasuredWidth() + left, redView.getMeasuredHeight());
		greenView.layout(left, redView.getBottom(), greenView.getMeasuredWidth() + left,
				redView.getBottom() + greenView.getMeasuredHeight());
	}

	/**
	 * 重写onFinishInflate()，当布局xml文件的结束标签被执行结束后，此时知道自己有几个字View， 一般用于初始化子View的引用
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		redView = getChildAt(0);
		greenView = getChildAt(1);
	}
}
