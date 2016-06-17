import java.text.DecimalFormat;
import java.util.Random;


public class NeuralNetwork {
	// ニューラルネットワーク
	// input  : "盤上のBlack(=1)が自分"の形式の盤
	// output : 盤の局面評価の価値

	final int biasSize = 1;
	final int bias = -1;

	final int WIH1row;
	final int WIH1col;
	final int WH1H2row;
	final int WH1H2col;
	final int WH2H3row;
	final int WH2H3col;
	final int WH3Orow;
	final int WH3Ocol;

	final int IL[];							//InputLayer
	final double HL1[];						//HiddenLayer1
	final double HL2[];						//HiddenLayer2
	final double HL3[];						//HiddenLayer3
	final double OL[];						//OutputLayer
	final double WIH1[][];					//Weight between Input and Hidden1
	final double WH1H2[][];					//Weight between Hidden1 and Hidden2
	final double WH2H3[][];					//Weight between Hidden2 and Hidden3
	final double WH3O[][];					//Weight between Hidden3 and Output
	// WeightはReadOnlyで十分なのでシャローコピーでよく、
	// bias追加部分はディープコピー(遅い)が必要なことに気を付ける

	Random rand = new Random();
	DecimalFormat df = new DecimalFormat("###.#######");


	NeuralNetwork(int IL_len, int HL1_len, int HL2_len, int HL3_len, int OL_len,
			double WIH1[][], double WH1H2[][], double WH2H3[][], double WH3O[][]){
		//IL[1,n] HM1[n,m] HL[1,m] HM2[m,l] ...
		this.IL  = new int[IL_len + biasSize];
		this.HL1 = new double[HL1_len];
		this.HL2 = new double[HL2_len];
		this.HL3 = new double[HL3_len];
		this.OL  = new double[OL_len];

		this.WIH1row  = WIH1.length + biasSize;
		this.WIH1col  = WIH1[0].length;
		this.WH1H2row = WH1H2.length;
		this.WH1H2col = WH1H2[0].length;
		this.WH2H3row = WH2H3.length;
		this.WH2H3col = WH2H3[0].length;
		this.WH3Orow  = WH3O.length;
		this.WH3Ocol  = WH3O[0].length;

		this.WIH1  = new double[WIH1row][WIH1col];
		for(int i=0;i<this.WIH1.length;i++) for(int j=0;j<this.WIH1[0].length;j++){
			if(i < this.biasSize) this.WIH1[i][j] = 1;
			else this.WIH1[i][j]  = WIH1[i - this.biasSize][j];
		}
		this.WH1H2 = WH1H2;
		this.WH2H3 = WH2H3;
		this.WH3O = WH3O;

		if(this.WIH1row  != this.IL.length)  System.out.println("WIH1row != IL_len");
		if(this.WIH1col  != this.HL1.length) System.out.println("WIH1col != HL1_len");
		if(this.WH1H2row != this.HL1.length) System.out.println("WH1H2row != HL1_len");
		if(this.WH1H2col != this.HL2.length) System.out.println("WH1H2col != HL2_len");
		if(this.WH2H3row != this.HL2.length) System.out.println("WH2H3row != HL2_len");
		if(this.WH2H3col != this.HL3.length) System.out.println("WH2H3col != HL3_len");
		if(this.WH3Orow  != this.HL3.length) System.out.println("WH3Orow != HL3_len");
		if(this.WH3Ocol  != this.OL.length)  System.out.println("WH3Ocol != OL_len");
		if(this.OL.length != 1) System.out.println("! OL.length != 1 !");
	}

	// アクティベイト関数
	// Standard Sigmoid Function
	public void Activate(double A[]){
		for(int j=0;j<A.length;j++){
			double alpha = 1;
			A[j] = 1.0 / (1.0 + Math.exp(-1.0 * alpha * A[j]));
		}
		return;
	}

	// NNの計算を実行する
	public double Execute(int[] Input){
		for(int i=0;i<this.IL.length;i++){
			if(i < this.biasSize) IL[i] = this.bias;
			else IL[i] = Input[i - this.biasSize];
		}

		for(int i=0;i<this.WIH1col;i++){
			this.HL1[i] = 0;
			for(int j=0;j<this.WIH1row;j++){
				this.HL1[i] += this.IL[j] * this.WIH1[j][i];
			}
		}
		this.Activate(this.HL1);

		for(int i=0;i<this.WH1H2col;i++){
			this.HL2[i] = 0;
			for(int j=0;j<this.WH1H2row;j++){
				this.HL2[i] += this.HL1[j] * this.WH1H2[j][i];
			}
		}
		this.Activate(this.HL2);

		for(int i=0;i<this.WH2H3col;i++){
			this.HL3[i] = 0;
			for(int j=0;j<this.WH2H3row;j++){
				this.HL3[i] += this.HL2[j] * this.WH2H3[j][i];
			}
		}
		this.Activate(this.HL3);

		for(int i=0;i<this.WH3Ocol;i++){
			this.OL[i] = 0;
			for(int j=0;j<this.WH3Orow;j++){
				this.OL[i] += this.HL3[j] * this.WH3O[j][i];
			}
		}
		//System.out.print(this.OL[0] + "   ->   ");
		this.Activate(this.OL);
		//System.out.println(this.OL[0]);

		//this.displayInputOutput();
		return OL[0];
	}

	// Input値とOutput値を出力する
	/*
	private void displayInputOutput(){
		System.out.print("IL" + " : ");
		//for(int i=0;i<this.IL.length;i++) System.out.print(df.format(this.IL[i]) + " ");
		System.out.print(" -> ");
		System.out.print("OL" + " : ");
		for(int i=0;i<this.OL.length;i++) System.out.print(df.format(this.OL[i]) + " "); System.out.println();
	}
	*/
}
