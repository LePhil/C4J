package de.andrena.next.acceptancetest.timeofday;

import static de.andrena.next.Condition.ignored;
import static de.andrena.next.Condition.post;
import static de.andrena.next.Condition.pre;
import static de.andrena.next.Condition.result;
import de.andrena.next.Condition;

public class RicherTimeOfDaySpecContract implements RicherTimeOfDaySpec {
	private RicherTimeOfDaySpec target = Condition.target();

	@Override
	public int getHour() {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
		return ignored();
	}

	@Override
	public int getMinute() {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
		return ignored();
	}

	@Override
	public int getSecond() {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
		return ignored();
	}

	@Override
	public void setHour(int hour) {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public void setMinute(int minute) {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public void setSecond(int second) {
		if (pre()) {
			// no further pre-condition identified yet
		}
		if (post()) {
			// no further post-condition identified yet
		}
	}

	@Override
	public int getNearestHour() {
		if (pre()) {
			// no pre-condition identified yet
		}
		if (post()) {
			int result = result(Integer.class);
			int hour = target.getHour();
			int minute = target.getMinute();
			assert !(minute < 30) || (result == hour) : "if minute < 30 then result == hour";
			assert !(minute >= 30 && hour < 23) || (result == hour + 1) : "if minute >= 30 && hour < 23 then result == hour + 1";
			assert !(minute >= 30 && hour == 23) || (result == 0) : "if minute >= 30 && hour == 23 then result == 23";
		}
		return ignored();
	}
}