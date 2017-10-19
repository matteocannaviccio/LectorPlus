package it.uniroma3.model.paper;

import java.io.IOException;
import org.apache.commons.math3.stat.interval.WilsonScoreInterval;

/**
 * Class used to calculate Wilson score on the extracted facts.
 * It takes in input only the files of the facts evaluated and 
 * calculate score for each relations found in the facts.
 * 
 * 
 * @author matteo
 *
 */
public class WilsonScore {

	private double confidence;

	public WilsonScore(double confidence){
		this.confidence = confidence;
	}

	public double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public String getScore(int total, int right){
		WilsonScoreInterval dd = new WilsonScoreInterval();
		double lowerB = dd.createInterval(total, right, this.confidence).getLowerBound();
		double upperB = dd.createInterval(total, right, this.confidence).getUpperBound();
		double score = (upperB + lowerB)/2 * 100;
		double error = (upperB - lowerB)/2 * 100;
		String out = round(score,2) + " +/- " + round(error, 2) + "%";
		return out;
	}

	

	public static void main(String[] args) throws IOException {
		WilsonScore scorer = new WilsonScore(0.95);
		System.out.println(scorer.getScore(100, 90));

	}

}
