'use strict';

class Util
{
	static MAX_I32 = 2**31 - 1;

	static getRandomInt32() {
		return Util.getRandomInt(0, Util.MAX_I32);
	}

	static getRandomInt(min, max) {
		min = Math.ceil(min);
		max = Math.floor(max);
		return Math.floor(Math.random() * (max - min) + min); // The maximum is exclusive and the minimum is inclusive
	}

	static getRandomIntExcept(min, max, except) {
		let v = Util.getRandomInt(min, max);
		while (v == except) {
			v = Util.getRandomInt(min, max);
		}

		return v;
	}
}

module.exports = Util;