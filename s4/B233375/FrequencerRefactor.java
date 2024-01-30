package s4.B233375; // ここは、かならず、自分の名前に変えよ。

import java.lang.*;
import s4.specification.*;

/*package s4.specification;
  ここは、１回、２回と変更のない外部仕様である。
  public interface FrequencerInterface {     // This interface provides the design for frequency counter.
  void setTarget(byte  target[]); // set the data to search.
  void setSpace(byte  space[]);  // set the data to be searched target from.
  int frequency(); //It return -1, when TARGET is not set or TARGET's length is zero
  //Otherwise, it return 0, when SPACE is not set or SPACE's length is zero
  //Otherwise, get the frequency of TAGET in SPACE
  int subByteFrequency(int start, int end);
  // get the frequency of subByte of taget, i.e target[start], taget[start+1], ... , target[end-1].
  // For the incorrect value of START or END, the behavior is undefined.
  }
*/

public class FrequencerRefactor implements FrequencerInterface {
	byte[] target;
	byte[] space;
	boolean targetReady = false;
	boolean spaceReady = false;
	private int spaceLength = 0;
	/// spaceの接尾語を辞書順が早い順に並べた配列(要素は開始場所のインデックス)
	int[] suffixArray;

	/**
	 * SuffixArrayの内容を標準出力に出力する
	 */
	private void printSuffixArray() {
		if (spaceReady) {
			for (int i = 0; i < spaceLength; i++) {
				int s = suffixArray[i];
				System.out.printf("suffixArray[%2d]=%2d:", i, s);
				for (int j = s; j < spaceLength; j++) {
					System.out.write(this.space[j]);
				}
				System.out.write('\n');
			}
		}
	}

	/**
	 * 2つの接尾文字列 S_i , S_jを辞書順に比較する
	 * ここで、S_iとはSpace Sのi番目(0-index)から始まる連続した文字列のことである
	 * e.g. S = "ABCD"のとき、 S_0 = "ABCD" , S_1 = "BCD" , S_2 = "CD" S_3 = "D" となる
	 * 文字列の比較は以下の順で決まる
	 * <ol>
	 * <li>先頭のアルファベット順 e.g. "a" < "b"
	 * <li>先頭のアルファベットと同じの時は次の文字列 e.g. "aa" < "ab"
	 * <li>一方の文字列がもう一方の接頭辞のときは文字列が長い方 e.g. "aa" < "aab"
	 * </ol>
	 * 
	 * @param i 比較する文字列1 (S="abcd", i=0 のとき、 S_i = "abcd")
	 * @param j 比較する文字列2 (S="abcd", j=2 のとき、 S_j = "cd")
	 * @return 大小比較の結果, S_i > S_j のとき、1以上の値を返し、S_i < S_j のとき、-1以下の値を返す S_i = S_j
	 *         のとき、0を返す
	 */
	private int suffixCompare(int i, int j) {

		int short_letter = i < j ? j : i;// 文字数が短い文字
		int comp = 0;// 比較結果
		int in_current = 0;// 現在見ている文字

		while (comp == 0) {// アルファベットが異なるまで繰り返す
			// 範囲内かどうか見る
			if (short_letter + in_current >= spaceLength) {// 表示範囲を超える
				comp = j - i;
				return comp;// 文字が長い方=開始インデックスが早い方を1とする
			}
			// 大小比較
			comp = this.space[i + in_current] - this.space[j + in_current];
			++in_current;
		}
		return comp;
	}

	public void setSpace(byte[] space) {
		// spaceに関する変数の定義
		this.space = space;
		spaceLength = this.space.length;
		if (spaceLength > 0)
			spaceReady = true;

		// suffixArrayの作成
		suffixArray = new int[spaceLength];
		// suffixArrayの初期化
		for (int i = 0; i < spaceLength; i++) {
			suffixArray[i] = i;
		}
		// suffixArrayをソートする
		MargeSort(0, spaceLength);
	}

	/**
	 * suffixArrayの半開区間[left, right)をマージソートによってソートする
	 * left = 4 , right = 7 の時, [4,7) = 4,5,6の要素についてソートを行う(終点は引数-1になることに注意)
	 * 
	 * @param left  区間の始点
	 * @param right 区間の終点
	 */
	private void MargeSort(int left, int right) {
		if (left + 1 >= right) {// 幅が1以下
			return;
		}
		if (left + 2 == right) {// 幅が2
			if (suffixCompare(left, left + 1) > 0) {// swap
				int tmpValue1 = suffixArray[left];
				int tmpValue2 = suffixArray[left + 1];
				suffixArray[left] = tmpValue2;
				suffixArray[left + 1] = tmpValue1;
			}
			return;
		}
		int length = right - left;
		int halfLength = length / 2;
		int middle = left + halfLength;

		MargeSort(left, middle);// 前半をソートする
		MargeSort(middle, right);// 後半をソートする

		// 一旦退避
		int tmp[] = new int[length];
		int count = 0;
		for (int i = left; i < right; ++i) {
			tmp[count++] = this.suffixArray[i];
		}

		int leftAt = 0, rightAt = halfLength;
		count = left;

		while (leftAt < halfLength && rightAt < length) {// どちらかが行き切るまで繰り返す
			if (suffixCompare(tmp[leftAt], tmp[rightAt]) > 0) {
				suffixArray[count] = tmp[rightAt++];
			} else {
				suffixArray[count] = tmp[leftAt++];
			}
			++count;
		}
		// この時点で左右どちらかが行き切るはず
		while (leftAt < halfLength) {// 左側をすべて行き切るまで
			suffixArray[count] = tmp[leftAt++];
			++count;
		}
		while (rightAt < length) {// 右側を行き切るまで
			suffixArray[count] = tmp[rightAt++];
			++count;
		}

	}
	// ここから始まり、指定する範囲までは変更してはならないコードである。

	public void setTarget(byte[] target) {
		this.target = target;
		if (this.target.length > 0)
			targetReady = true;
	}

	public int frequency() {
		if (targetReady == false)
			return -1;
		if (spaceReady == false)
			return 0;
		return subByteFrequency(0, this.target.length);
	}

	public int subByteFrequency(int start, int end) {

		int first = lowerBound(start, end);
		int last = upperBound(start, end);
		return last - first;// 半開区間で定義すれば良さそう
	}
	// 変更してはいけないコードはここまで。

	private int targetCompare(int i, int j, int k) {
		// subByteStartIndexとsubByteEndIndexを定義するときに使う比較関数。
		// 次のように定義せよ。
		// suffix_i is a string starting with the position i in "byte [] mySpace".:
		// mySpace[i,mySpace.length)
		// When mySpace is "ABCD", suffix_0 is "ABCD", suffix_1 is "BCD",
		// suffix_2 is "CD", and sufffix_3 is "D".
		// target_j_k is a string in myTarget start at j-th postion ending k-th
		// position. : myTarget[j,k)
		// if myTarget is "ABCD",
		// j=0, and k=1 means that target_j_k is "A".
		// j=1, and k=3 means that target_j_k is "BC".
		// This method compares suffix_i and target_j_k.
		// if the beginning of suffix_i matches target_j_k, it return 0.
		// if suffix_i > target_j_k it return 1;
		// if suffix_i < target_j_k it return -1;
		// if first part of suffix_i is equal to target_j_k, it returns 0;
		//
		// Example of search
		// suffix target
		// "o" > "i"
		// "o" < "z"
		// "o" = "o"
		// "o" < "oo"
		// "Ho" > "Hi"
		// "Ho" < "Hz"
		// "Ho" = "Ho"
		// "Ho" < "Ho " : "Ho " is not in the head of suffix "Ho"
		// "Ho" = "H" : "H" is in the head of suffix "Ho"
		// The behavior is different from suffixCompare on this case.
		// For example,
		// if suffix_i is "Ho Hi Ho", and target_j_k is "Ho",
		// targetCompare should return 0;
		// if suffix_i is "Ho Hi Ho", and suffix_j is "Ho",
		// suffixCompare should return 1. (It was written -1 before 2021/12/21)
		//
		// ここに比較のコードを書け
		// # TODO : ここの返り値は符号と大小を区別する形でできそう
		// サイズをtargetの長さに制限して、文字列比較?
		int comp_targetLength = k - j;
		int comp_spaceLength = spaceLength - suffixArray[i];
		int comp = 0;// 比較結果
		int in_current = 0;// 現在見ている文字
		int short_letter_length = comp_targetLength < comp_spaceLength ? comp_targetLength : comp_spaceLength;// 文字数が短い方の文字数
		while (comp == 0) {
			if (in_current >= short_letter_length) {// どちらかの最終文字を超えた時
				if (comp_spaceLength < comp_targetLength) {
					return -1;
				}
				return 0;
			}
			comp = this.space[suffixArray[i] + in_current] - this.target[j + in_current];

			++in_current;
		}
		return comp;
	}

	private int lowerBound(int start, int end) {
		// suffix arrayのなかで、目的の文字列の出現が始まる位置を求めるメソッド=lowerbound
		// 以下のように定義せよ。
		// The meaning of start and end is the same as subByteFrequency.
		/*
		 * Example of suffix created from "Hi Ho Hi Ho"
		 * 0: Hi Ho
		 * 1: Ho
		 * 2: Ho Hi Ho
		 * 3:Hi Ho
		 * 4:Hi Ho Hi Ho
		 * 5:Ho
		 * 6:Ho Hi Ho
		 * 7:i Ho
		 * 8:i Ho Hi Ho
		 * 9:o
		 * 10:o Hi Ho
		 */

		// It returns the index of the first suffix
		// which is equal or greater than target_start_end.
		// Suppose target is set "Ho Ho Ho Ho"
		// if start = 0, and end = 2, target_start_end is "Ho".
		// if start = 0, and end = 3, target_start_end is "Ho ".
		// Assuming the suffix array is created from "Hi Ho Hi Ho",
		// if target_start_end is "Ho", it will return 5.
		// Assuming the suffix array is created from "Hi Ho Hi Ho",
		// if target_start_end is "Ho ", it will return 6.
		//
		// ここにコードを記述せよ。
		//
		// int res; for (res = 0; res < spaceLength && (targetCompare(res, start, end)
		// == -1); ++res) { }
		// # TODO : 関数名を変更したい
		// 範囲が1以下のときは左端を返す

		int left = 0, right = spaceLength, middle;
		if (right - left <= 0) {
			return left;
		}
		// if (targetCompare(left, start, end) != -1) -> 0 or 1 +
		if (targetCompare(left, start, end) >= 0) {// 右端は明らかに対象よりも後に来るので、左端が対象よりも先に来ることを確認
			return left;// もし、対象と同じか、その後に来るなら、全部対象の後に来ることが分かるので、左端を返す
		}

		int comp;
		do {
			// 左端は対象よりも前に、右端は対象と同じか後ろに来るように持つ
			middle = left + (right - left) / 2;
			comp = targetCompare(middle, start, end);
			// if (comp == -1) , -1 - 2 ...
			if (comp < 0) {// 中央が対象より先に来るか、後に来るかで場合分け
				left = middle;
			} else {
				right = middle;
			}

		} while (right - left >= 2);// 幅が最小になるまで繰り返す
		return right; // 右端は対象と同じかその後に来る
	}

	private int upperBound(int start, int end) {
		// suffix arrayのなかで、目的の文字列の出現しなくなる場所を求めるメソッド=upper bound
		// 以下のように定義せよ。
		// The meaning of start and end is the same as subByteFrequency.
		/*
		 * Example of suffix created from "Hi Ho Hi Ho"
		 * 0: Hi Ho
		 * 1: Ho
		 * 2: Ho Hi Ho
		 * 3:Hi Ho
		 * 4:Hi Ho Hi Ho
		 * 5:Ho
		 * 6:Ho Hi Ho
		 * 7:i Ho
		 * 8:i Ho Hi Ho
		 * 9:o
		 * 10:o Hi Ho
		 */
		// It returns the index of the first suffix
		// which is greater than target_start_end; (and not equal to target_start_end)
		// Suppose target is set "High_and_Low",
		// if start = 0, and end = 2, target_start_end is "Hi".
		// if start = 1, and end = 2, target_start_end is "i".
		// Assuming the suffix array is created from "Hi Ho Hi Ho",
		// if target_start_end is "Ho", it will return 7 for "Hi Ho Hi Ho".
		// Assuming the suffix array is created from "Hi Ho Hi Ho",
		// if target_start_end is"i", it will return 9 for "Hi Ho Hi Ho".
		//
		// int res; for (res = spaceLength - 1; res >= 0 && (targetCompare(res, start,
		// end) == 1); --res) {}
		// # TODO : 関数名を変更したい
		int left = 0, right = spaceLength, middle;
		if (right - left <= 0) {
			return left;
		}
		// if (targetCompare(left, start, end) == 1) -> 1 , 2,
		if (targetCompare(left, start, end) > 0) {// 右端は明らかに対象の後に来るので、左端が対象と同じかその前に来ることを確認する
			return left;// もし対象の後に後にくるならば、すべてが対象の後ろに来ることが明らかなので左端を返す
		}
		int comp;
		do {
			// 左端は対象と同じかより前に来るように、右端は対象の後ろに来るように持っておく
			middle = left + (right - left) / 2;
			comp = targetCompare(middle, start, end);
			// if (comp == 1)// 1 , 2 , ...
			if (comp > 0) {
				right = middle;
			} else {
				left = middle;
			}

		} while (right - left >= 2);// 幅が最小になるまで繰り返す
		return right; // 右端は対象よりも後に来るはず
	}

	public static void main(String[] args) {
		FrequencerRefactor frequencerObject;
		try { // テストに使うのに推奨するmySpaceの文字は、"ABC", "CBA", "HHH", "Hi Ho Hi Ho".
			frequencerObject = new FrequencerRefactor();
			frequencerObject.setSpace("ABC".getBytes());
			frequencerObject.printSuffixArray();
			// test for suffix array of ABC (ABC , BC ,A -> 0,1,2)
			int[] expect_suffix_abc = { 0, 1, 2 };
			for (int i = 0; i < frequencerObject.suffixArray.length; ++i) {
				System.err.printf("suffix[%d] : (except %d , actually %d) :", i, expect_suffix_abc[i],
						frequencerObject.suffixArray[i]);
				if (expect_suffix_abc[i] != frequencerObject.suffixArray[i]) {
					System.err.println("ng");
				} else {
					System.err.println("ok");
				}
			}
			frequencerObject = new FrequencerRefactor();
			frequencerObject.setSpace("CBA".getBytes());
			frequencerObject.printSuffixArray();
			int[] expect_suffix_cba = { 2, 1, 0 };// A BA CBA
			for (int i = 0; i < frequencerObject.suffixArray.length; ++i) {
				System.err.printf("suffix[%d] : (except %d , actually %d) :", i, expect_suffix_cba[i],
						frequencerObject.suffixArray[i]);
				if (expect_suffix_cba[i] != frequencerObject.suffixArray[i]) {
					System.err.println("ng");
				} else {
					System.err.println("ok");
				}
			}
			frequencerObject = new FrequencerRefactor();
			frequencerObject.setSpace("HHH".getBytes());
			frequencerObject.printSuffixArray();
			int[] expect_suffix_hhh = { 2, 1, 0 };// H HH HHH
			for (int i = 0; i < frequencerObject.suffixArray.length; ++i) {
				System.err.printf("suffix[%d] : (except %d , actually %d) :", i, expect_suffix_hhh[i],
						frequencerObject.suffixArray[i]);
				if (expect_suffix_hhh[i] != frequencerObject.suffixArray[i]) {
					System.err.println("ng");
				} else {
					System.err.println("ok");
				}
			}
			System.out.println();
			frequencerObject = new FrequencerRefactor();
			frequencerObject.setSpace("Hi Ho Hi Ho".getBytes());
			frequencerObject.printSuffixArray();
			/*
			 * Example from "Hi Ho Hi Ho"
			 * 0: Hi Ho
			 * 1: Ho
			 * 2: Ho Hi Ho
			 * 3:Hi Ho
			 * 4:Hi Ho Hi Ho
			 * 5:Ho
			 * 6:Ho Hi Ho
			 * 7:i Ho
			 * 8:i Ho Hi Ho
			 * 9:o
			 * 10:o Hi Ho
			 */ int[] expect_suffix_long = { 5, 8, 2, 6, 0, 9, 3, 7, 1, 10, 4 };// H HH HHH
			for (int i = 0; i < frequencerObject.suffixArray.length; ++i) {
				System.err.printf("suffix[%d] : (except %d , actually %d) :", i, expect_suffix_long[i],
						frequencerObject.suffixArray[i]);
				if (expect_suffix_long[i] != frequencerObject.suffixArray[i]) {
					System.err.println("ng");
				} else {
					System.err.println("ok");
				}
			}
			// 1文字でのテスト
			frequencerObject.setSpace("Hi Ho Hi Ho".getBytes());
			frequencerObject.printSuffixArray();
			frequencerObject.setTarget("H".getBytes());
			int result = frequencerObject.frequency();
			int except_value = 4;
			System.out.print(frequencerObject.target.toString() + " in " + frequencerObject.space.toString()
					+ " : Freq = " + result + " ");
			if (except_value == result) {
				System.out.println("OK");
			} else {
				System.out.println("WRONG");
			}
			// 2文字でのテスト
			frequencerObject.setSpace("Hi Ho Hi Ho".getBytes());
			System.out.println("space length:" + frequencerObject.space.length);
			frequencerObject.printSuffixArray();
			frequencerObject.setTarget("Hi".getBytes());
			result = frequencerObject.frequency();
			except_value = 2;
			System.out.print("Freq = " + result + " ");
			if (except_value == result) {
				System.out.println("OK");
			} else {
				System.out.println("WRONG ,value=" + result);
			}
			// 探索地点が終点を超えるときのテスト
			frequencerObject.setTarget("z".getBytes());
			result = frequencerObject.frequency();
			except_value = 0;
			System.out.print("Freq = " + result + " ");
			if ((except_value == result) && (frequencerObject.upperBound(0,
					frequencerObject.space.length) == frequencerObject.space.length)) {
				System.out.println("OK");
			} else {
				System.out.println("WRONG value : " + result + " end pos : " + frequencerObject.upperBound(0,
						frequencerObject.space.length));
			}
			// 探索位置が始点のときのテスト
			frequencerObject.setSpace("hello".getBytes());
			frequencerObject.printSuffixArray();
			frequencerObject.setTarget("a".getBytes());
			result = frequencerObject.frequency();
			except_value = 0;
			System.out.print("Freq = " + result + " ");
			if ((except_value == result) && (frequencerObject.lowerBound(0,
					frequencerObject.space.length) == 0)) {
				System.out.println("OK");
			} else {
				System.out.println("WRONG");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("STOP");
		}
	}
}
