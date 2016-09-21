package com.lmoh.qqslidemenu.test;

import java.util.Random;

import com.lmoh.qqslidemenu.R;
import com.lmoh.qqslidemenu.test.SlideMenu.OnDragStateChangeListener;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private ListView menu_listview;
	private ListView main_listview;
	private SlideMenu slidemenu;
	private ImageView iv_head;
	private MyLinearLayout my_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}

	

	private void initView() {
		setContentView(R.layout.activity_main);
		menu_listview = (ListView) findViewById(R.id.menu_listview);
		main_listview = (ListView) findViewById(R.id.main_listview);
		slidemenu = (SlideMenu) findViewById(R.id.slidemenu);
		iv_head = (ImageView) findViewById(R.id.iv_head);
		my_layout = (MyLinearLayout) findViewById(R.id.my_layout);
	}
	
private void initData() {
	my_layout.setSlideMenu(slidemenu);
	menu_listview.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Constant.sCheeseStrings){
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view =  (TextView) super.getView(position, convertView, parent);
			view.setTextColor(Color.WHITE);
			return view;
		}
	});
	main_listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,Constant.NAMES){
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view;
			if (convertView != null) {
				view = (TextView) convertView;
			}else{
				view =  (TextView) super.getView(position, convertView, parent);
			}
			
			//item条目添加动画
			//1.缩小
			ViewHelper.setScaleX(view, 0.5f);
			ViewHelper.setScaleY(view, 0.5f);
			//2.以属性动画放大
			ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350).start();
			ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350).start();
			return view;
		}
	});
	slidemenu.setOnDragStateChangeListener(new OnDragStateChangeListener() {
		
		@Override
		public void onOpen() {
			System.out.println("onOpen");
			//打开菜单时，让listview跳到随机条目
			menu_listview.smoothScrollToPosition(new Random().nextInt(menu_listview.getCount()));
		}
		
		@Override
		public void onDraging(float fraction) {
			//图片越变透明
			ViewHelper.setAlpha(iv_head,1-fraction);
			if (fraction == 0) {
				ViewPropertyAnimator.animate(iv_head).translationXBy(15)
				.setInterpolator(new CycleInterpolator(4))
				.setDuration(500)
				.start();
			}else if (fraction == 1f) {
				menu_listview.smoothScrollToPosition(new Random().nextInt(menu_listview.getCount()));
			}
		}
		
		@Override
		public void onClose() {
			System.out.println("onClose");
			//关闭时，图片抖动动画
			ViewPropertyAnimator.animate(iv_head).translationXBy(15)
			.setInterpolator(new CycleInterpolator(4))
			.setDuration(500)
			.start();
		}
	});
	
	}
}
