package de.vksi.c4j.internal.compiler;

public class BooleanExp extends NestedExp {
	public static final BooleanExp TRUE = new BooleanExp(Boolean.TRUE.toString());
	public static final BooleanExp FALSE = new BooleanExp(Boolean.FALSE.toString());

	private String code;

	public BooleanExp(NestedExp exp) {
		this(exp.getCode());
	}

	public static BooleanExp valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	protected BooleanExp(String code) {
		this.code = code;
	}

	public BooleanExp and(NestedExp exp) {
		return new BooleanExp("(" + code + " && " + exp.getCode() + ")");
	}

	public BooleanExp or(NestedExp exp) {
		return new BooleanExp("(" + code + " || " + exp.getCode() + ")");
	}

	public BooleanExp not() {
		return new BooleanExp("!(" + code + ")");
	}

	@Override
	protected String getCode() {
		return code;
	}

}
