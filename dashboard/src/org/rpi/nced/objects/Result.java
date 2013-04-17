package org.rpi.nced.objects;

import java.util.ArrayList;

public class Result implements Comparable<Result> {
	ArrayList<Binding> binding;

	public ArrayList<Binding> getBinding() {
		return binding;
	}

	public void setBinding(ArrayList<Binding> binding) {
		this.binding = binding;
	}

	@Override
	public int compareTo(Result result) {
		if (binding != null && result.getBinding() != null) {
			String currentTitle = getTitleFromBinding(binding);
			
			String previousTitle = getTitleFromBinding(result.getBinding());
			return currentTitle.compareTo(previousTitle);
		}
		return -1;
	}

	private String getTitleFromBinding(ArrayList<Binding> previousBinding) {
		String title = "";
		for(Binding b : previousBinding){
			if(b.getName().equals("title")){
				title = b.getLiteral();
			}
		}
		return title;
	}

}
