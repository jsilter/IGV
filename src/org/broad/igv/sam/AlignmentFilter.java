package org.broad.igv.sam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AlignmentFilter {

    private boolean exclude;
    private String tag;
    private String expression;
    private Operator comparisonOperator = Operator.EQUAL;

    public boolean isNumericComparison() {
        return comparisonOperator == Operator.GREATER_THAN ||
                comparisonOperator == Operator.LESS_THAN ||
                comparisonOperator == Operator.EQUAL;
    }

    public static enum Operator {

        EQUAL("is equal to"), NOT_EQUAL("is not equal to"), GREATER_THAN(
                "is greater than"), LESS_THAN("is less than"), STARTS_WITH("starts with"), CONTAINS(
                "contains"), REGULAR_EXPRESSION("is regular expression"), CONTAINS_WILDCARDS(
                "contains wild cards"), IS_MISSING("is missing"), DOES_NOT_CONTAIN(
                "does not contain");

        String displayValue;

        Operator(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }

        static public Operator findEnum(String displayValue) {

            if (displayValue == null) {
                return null;
            }

            for(Operator o: Operator.values()){
                if(o.getDisplayValue().equals(displayValue)){
                    return o;
                }
            }

            return null;
        }
    }


//    public static enum BooleanOperator {
//
//        AND("AND"), OR("OR");
//
//        String value;
//
//        BooleanOperator(String value) {
//            this.value = value;
//        }
//
//        public String getValue() {
//            return value;
//        }
//
//        static public BooleanOperator findEnum(String value) {
//
//            if (value == null) {
//                return null;
//            }
//
//            if (value.equals(AND.getValue())) {
//                return AND;
//            } else if (value.equals(OR.getValue())) {
//                return OR;
//            }
//            return null;
//        }
//    }

    public boolean isExclude() {
		return exclude;
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	AlignmentFilter() {
		exclude = true;
		tag = "NM";
		expression = "*T*";
	}

	public String getTag() {
		return tag;
	}

    /**
     * Apply this filter instance to {@code value}.
     *
     * @param value True to include, false to exclude
     * @return
     */
	public boolean filter(String value) {
		String regExprToUse = null;
		Pattern regExpr;

		boolean match = true;

        //Check if numeric filter
        if(isNumericComparison()){
            return filter(Double.parseDouble(value));
        }

        switch(comparisonOperator){
            case EQUAL:
                match = value.equalsIgnoreCase(expression);
                break;
            case NOT_EQUAL:
                match = !value.equalsIgnoreCase(expression);
                break;
            case STARTS_WITH:
                match = value.startsWith(expression);
                break;
            case CONTAINS:
                match = value.contains(expression);
                break;
            case DOES_NOT_CONTAIN:
                match = !value.contains(expression);
                break;
            case REGULAR_EXPRESSION:
                regExprToUse = expression;
                regExpr = Pattern.compile(regExprToUse);
                Matcher matcher = regExpr.matcher(value);
                match = matcher.matches();
                break;
            case IS_MISSING:
                match = value == null;
                break;
        }

        match = exclude ? !match : match;

		return match;
	}

	public boolean filter(double attr) {
        if(!isNumericComparison()){
            return filter("" + attr);
        }
		if (comparisonOperator == Operator.GREATER_THAN) {
			return Double.parseDouble(expression) >= attr;
		} else if (comparisonOperator == Operator.LESS_THAN) {
			return Double.parseDouble(expression) <= attr;
		} else if (comparisonOperator == Operator.EQUAL){
            return Double.parseDouble(expression) == attr;
        }

        throw new IllegalStateException("This is a programming error, somehow we treated this filter as both numeric and non-numeric");
	}

	public void setComparisonOperator(Operator op) {
		this.comparisonOperator = op;
	}

    public void setComparisonOperator(String s){
        this.setComparisonOperator(Operator.findEnum(s));
    }

	public Operator getComparisonOperator() {
		return this.comparisonOperator;
	}

}
