package modules.communication.json.beans;

/**
 * Bean holding information about a platform under an agent's control
 * @author Marc Frincu
 * @since 2010
 */
public class PlatformInfo {

	double heterogeneityFactor, avgTaskSize, avgTaskEET, avgStdDevTaskEET, avgStdDevTaskSize;
	int noTasks, noLongTasks, noResources;

	public PlatformInfo() {
		
	}
	
	public PlatformInfo(
			double heterogeneityFactor,
			double avgTaskSize,
			double avgTaskEET,
			double avgStdDevTaskEET,
			double avgStdDevTaskSize,
			int noTasks,
			int noLongTasks,
			int noResources
			) {
		this.heterogeneityFactor = heterogeneityFactor;
		this.avgStdDevTaskEET = avgStdDevTaskEET;
		this.avgStdDevTaskSize = avgStdDevTaskSize;
		this.avgTaskEET = avgTaskEET;
		this.avgTaskSize = avgTaskSize;
		this.noLongTasks = noLongTasks;
		this.noTasks = noTasks;
		this.noResources = noResources;
	}

	public double getHeterogeneityFactor() {
		return heterogeneityFactor;
	}

	public void setHeterogeneityFactor(double heterogeneityFactor) {
		this.heterogeneityFactor = heterogeneityFactor;
	}

	public double getAvgTaskSize() {
		return avgTaskSize;
	}

	public void setAvgTaskSize(double avgTaskSize) {
		this.avgTaskSize = avgTaskSize;
	}

	public double getAvgTaskEET() {
		return avgTaskEET;
	}

	public void setAvgTaskEET(double avgTaskEET) {
		this.avgTaskEET = avgTaskEET;
	}

	public double getAvgStdDevTaskEET() {
		return avgStdDevTaskEET;
	}

	public void setAvgStdDevTaskEET(double avgStdDevTaskEET) {
		this.avgStdDevTaskEET = avgStdDevTaskEET;
	}

	public double getAvgStdDevTaskSize() {
		return avgStdDevTaskSize;
	}

	public void setAvgStdDevTaskSize(double avgStdDevTaskSize) {
		this.avgStdDevTaskSize = avgStdDevTaskSize;
	}

	public int getNoTasks() {
		return noTasks;
	}

	public void setNoTasks(int noTasks) {
		this.noTasks = noTasks;
	}

	public int getNoLongTasks() {
		return noLongTasks;
	}

	public void setNoLongTasks(int noLongTasks) {
		this.noLongTasks = noLongTasks;
	}

	public int getNoResources() {
		return noResources;
	}

	public void setNoResources(int noResources) {
		this.noResources = noResources;
	}
}
