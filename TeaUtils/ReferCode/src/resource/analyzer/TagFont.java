package resource.analyzer;

import org.htmlparser.tags.CompositeTag;

public class TagFont extends CompositeTag{
	private static final String mIds[] = {
			"font","FONT"
		};
		private static final String mEndTagEnders[] = {
			"font","FONT"
		};

		public TagFont(){
		}

		public String[] getIds(){
			return mIds;
		}
		public String[] getEndTagEnders(){
			return mEndTagEnders;
		}

		@Override
		public String getText() {
			
			return this.getChildren().elementAt(0).getText();
		}
		
		
}
