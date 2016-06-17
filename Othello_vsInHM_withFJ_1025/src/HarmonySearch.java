import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;


public class HarmonySearch {
	// ハーモニーサーチアルゴリズム
	// 日本語の説明の文献が見つからないので英語でコメント

	int row1, col1;
	int row2, col2;
	int row3, col3;
	int row4, col4;
	double HM1[][][];			// HarmonyMemory HM[hms][InputArrayRow][InputArrayCol]
	double HM2[][][];			//
	double HM3[][][];			//
	double HM4[][][];			//
	double HM1TF[][];			// UseInput 0/1 HM[hms][InputArrayRow][InputArrayCol]
	double F[];					// Fitness F[hms] (F[i] = f(HM1[i], HM2[i], HM2[i]))

	double NewH1[][][];			// New(Candidate for NextGeneration) Harmony
	double NewH2[][][];			//
	double NewH3[][][];			//
	double NewH4[][][];			//
	double NewF[];					// Fitness

	int hms;						// Harmony Memory Size
	double hmcr = 0.95;				// Harmony Change Rate (default = 0.95)
	double par = 0.33;				// Pitch Adjustment Rate (default = 0.33)
	double fw[] = { 0.005, 0.0 };	// Fret Width (need tuning carefully,
									// default: (0.01 × allowed range) to (0.001 × allowed range)
	int mi;							// Maximum Improvisation

	double ranmax = 1.0;		// Maxmum Generated by Random (need tuning carefully)
	double ranmin = -1.0;		// Minmum Generated by Random (need tuning carefully)

	Random rand = new Random();
	DecimalFormat df = new DecimalFormat("###.##");


	HarmonySearch(int HarmonyMemorySize, int MaximumImprovisation, Boolean[][] HM1TF,
			int IL_len, int HL1_len, int HL2_len, int HL3_len, int OL_len){
		this.hms = HarmonyMemorySize;
		this.mi = MaximumImprovisation;

		//IL[1,n] HM1[n,m] HL[1,m] HM2[m,l] OL[1,l]
		this.row1 = IL_len;
		this.col1 = HL1_len;
		this.row2 = HL1_len;
		this.col2 = HL2_len;
		this.row3 = HL2_len;
		this.col3 = HL3_len;
		this.row4 = HL3_len;
		this.col4 = OL_len;
		this.HM1 = new double[this.hms][this.row1][this.col1];
		this.HM1TF = new double[this.row1][this.col1];
		this.HM2 = new double[this.hms][this.row2][this.col2];
		this.HM3 = new double[this.hms][this.row3][this.col3];
		this.HM4 = new double[this.hms][this.row4][this.col4];
		this.F = new double[this.hms];
		this.NewH1 = new double[this.hms][this.row1][this.col1];
		this.NewH2 = new double[this.hms][this.row2][this.col2];
		this.NewH3 = new double[this.hms][this.row3][this.col3];
		this.NewH4 = new double[this.hms][this.row4][this.col4];
		this.NewF = new double[this.hms];

		for(int i=0;i<this.row1;i++) for(int j=0;j<this.col1;j++){
			this.HM1TF[i][j] = HM1TF[i][j] ? 1 : 0;
		}
	}

	// Calculate Fitness
	public double CalcF(Pii result){
		double res = 0;
		int pcnt = result.first;
		int ccnt = result.second;
		if(pcnt == ccnt) res =  1;
		if(pcnt > ccnt)  res = 10;
		if(pcnt < ccnt)	 res =  0;
		return res;
	}

	// Normalization
	private void Normalization(double A[][][], double TF[][]){
		for(int k=0;k<A.length;k++) for(int i=0;i<A[0].length;i++) for(int j=0;j<A[0][0].length;j++)
			A[k][i][j] *= TF[i][j];
		return;
	}

	// Randomize
	private void Randomize(double A[][][]){
		int p = A.length;
		int row = A[0].length;
		int col = A[0][0].length;
		for(int k=0;k<p;k++) for(int i=0;i<row;i++) for(int j=0;j<col;j++) A[k][i][j] = rand.nextDouble();
		return;
	}

	// RandomTuning(Generate)
	public void RandomTuning(){
		this.Randomize(this.HM1);
		this.Normalization(this.HM1, this.HM1TF);
		this.Randomize(this.HM2);
		this.Randomize(this.HM3);
		this.Randomize(this.HM4);
		return;
	}

	// Accidentaling
	public void Accidentaling(Computer_Normal COM_AI){
		System.out.println("No Accidentaling");
		return;
	}

	// Update HM
	public void MemoryUpdate(){
		for(int k=0;k<this.hms;k++){
			double fnew = this.NewF[k];
			int worst = 0, best = 0;
			for(int i=0;i<this.hms;i++){
				if(F[i] < F[worst]) worst = i;
				if(F[i] > F[best]) best = i;
			}
			//if(F[best] <= fnew) this.Accidentaling();
			if(F[worst] <= fnew){
				for(int i=0;i<this.NewH1[k].length;i++) for(int j=0;j<this.NewH1[k][0].length;j++) this.HM1[worst][i][j] = this.NewH1[k][i][j];
				for(int i=0;i<this.NewH2[k].length;i++) for(int j=0;j<this.NewH2[k][0].length;j++) this.HM2[worst][i][j] = this.NewH2[k][i][j];
				for(int i=0;i<this.NewH3[k].length;i++) for(int j=0;j<this.NewH3[k][0].length;j++) this.HM3[worst][i][j] = this.NewH3[k][i][j];
				for(int i=0;i<this.NewH4[k].length;i++) for(int j=0;j<this.NewH4[k][0].length;j++) this.HM4[worst][i][j] = this.NewH4[k][i][j];
				this.F[worst] = fnew;
			}
		}
		return;
	}

	// Generate next generation
	public void HarmonyImprovization(){
		this.NewH1 = this.HarmonyImprovization_part(HM1);
		this.Normalization(this.NewH1, this.HM1TF);
		this.NewH2 = this.HarmonyImprovization_part(HM2);
		this.NewH3 = this.HarmonyImprovization_part(HM3);
		this.NewH4 = this.HarmonyImprovization_part(HM4);

		for(int k=0;k<this.hms;k++) this.NewF[k] = 0;
		return;
	}
	private double[][][] HarmonyImprovization_part(double A[][][]){
		int hms = A.length;
		int row = A[0].length;
		int col = A[0][0].length;
		double NewH[][][] = new double[hms][row][col];

		for(int k=0;k<hms;k++){
			for(int i=0;i<row;i++) for(int j=0;j<col;j++){
				double ran_hmcr = rand.nextDouble();
				if(ran_hmcr < 1.0 - this.hmcr){
					double ran = rand.nextDouble();
					NewH[k][i][j] = (int)(this.ranmin + (this.ranmax - this.ranmin) * ran);
				}else{
					double ran = rand.nextDouble();
					int k2 = (int)(ran * this.hms);
					NewH[k][i][j] = A[k2][i][j];
					double ran_par = rand.nextDouble();
					if(ran_par < this.par){
						double ran_delta = rand.nextDouble();
						double delta = -this.fw[0] + ran_delta * 2 * this.fw[0];
						NewH[k][i][j] += delta;
						this.Scaling(NewH[k][i][j]);
					}else{
					}
				}
			}
		}
		return NewH;
	}

	// Scaling
	private double Scaling(double x){
		if(x < this.ranmin) x = this.ranmin;
		if(x > this.ranmax) x = this.ranmax;
		return x;
	}

	// display HarmonyMemory and Fitness
	public void displayF(){
		System.out.println("F (with sort) :");
		double tmp[] = new double[this.hms];
		for(int i=0;i<this.hms;i++) tmp[i] = F[i];
		Arrays.sort(tmp);
		for(int i=this.hms-1;i>=0;i--) System.out.println(" " + tmp[i]);
		System.out.println();
		return;
	}

	// display the best Harmony
	public void displayBest(){
		int i2 = 0;
		for(int i=0;i<this.hms;i++) if(F[i] > F[i2]) i2 = i;
		System.out.println("HM1-4[best][0][0] [0][1]");
		System.out.println(df.format(this.HM1[i2][0][0]) + " " + df.format(this.HM1[i2][0][1]));
		System.out.println(df.format(this.HM2[i2][0][0]) + " " + df.format(this.HM2[i2][0][1]));
		System.out.println(df.format(this.HM3[i2][0][0]) + " " + df.format(this.HM3[i2][0][1]));
		System.out.println(df.format(this.HM4[i2][0][0]));
		System.out.println("No." + i2 + " F : " + F[i2]);
		System.out.println();
		return;
	}

	// display F and NewF
	public void displayFandNewF(){
		System.out.print("Old F :  ");
		for(int k=0;k<this.hms;k++) System.out.print(df.format(this.F[k]) + ", ");
		System.out.println();

		System.out.print("New F :  ");
		for(int k=0;k<this.hms;k++) System.out.print(df.format(this.NewF[k]) + ", ");
		System.out.println();
	}
}