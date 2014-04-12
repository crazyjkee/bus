package com.bus.andraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class CustomDialog extends AlertDialog.Builder implements DialogInterface.OnMultiChoiceClickListener,DialogInterface.OnClickListener{
	public static interface OnDissDialog{
		public void _onDiss();
	}
	HashMap<String,LatLng> hm;
	ArrayList<String> ar,ardel;
	protected boolean[] _selections;
	protected CharSequence[] name;
	private OnDissDialog ondiss=null;
	
	public void setOnDissDialog(OnDissDialog odiss){
		this.ondiss = odiss;
	}

	public CustomDialog(Context context) {
		super(context);	
		
		this.setNegativeButton("Clear", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialogInterface, int arg1) {
				hm.clear();	
				if(ondiss!=null)
					ondiss._onDiss();

			}
		});
		this.setPositiveButton("Delete", this);
	}
	private void init(){
		ar =  new ArrayList<String>();
		ardel = new ArrayList<String>();
		_selections = new boolean[this.hm.size()];
		for(String k:this.hm.keySet()){
			Log.d("myLogs","keySet:"+k);
			ar.add(k);}
		Log.d("myLogs","hm.size:"+hm.size());
		name = ar.toArray(new CharSequence[hm.size()]);
		Log.d("myLogs","SIZE:selection:"+_selections.length+"name:"+name.length);
	}

	@Override
	public AlertDialog create() {
		this.setTitle("Your points");
		this.setMultiChoiceItems(name, _selections, this);
		return super.create();
	}

	@Override
	public void onClick(DialogInterface dialogInterface, int item, boolean b) {
		Log.d("myLogs",String.format("%s: %s", name[item],b));
		if(b==true&&!ardel.contains(name[item])){
		ardel.add(name[item].toString());
		for(String s:ardel)
			Log.d("myLogs","delete:"+s);}
		if(b==false&&ardel.contains(name[item]))
			ardel.remove(name[item].toString());
		
	}

	public HashMap<String, LatLng> getHm() {
		return hm;
	}

	public void setHm(HashMap<String, LatLng> hm) {
		this.hm = hm;
		init();
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		Set<String> keySet = hm.keySet();
		Iterator<String> keySetIterator = keySet.iterator();
		String key;
		while(keySetIterator.hasNext()){
			key = keySetIterator.next();
			for(String a:ardel){
				if(a.equals(key)){
					Log.d("myLogs","key:"+key);
					keySetIterator.remove();}}
			}
			if(ondiss!=null)
				ondiss._onDiss();

			
			
		
	}


}
