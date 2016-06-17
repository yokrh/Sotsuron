
public class Methods{
	// 補助メソッド群。未使用

	Methods(){}

	// swap
	public void swap(int x, int y){
		int tmp = x;
		x = y;
		y = tmp;
		return;
	}

	//A[i],A[j]をswap
	public void swap(int A[], int i, int j){
		int tmp = A[i];
		A[i] = A[j];
		A[j] = tmp;
		return;
	}

	//A[],B[]をswap
	public void swap(int A[], int B[]){
		if(A.length != B.length) return;
		int n = A.length;
		int Tmp[] = new int[n];
		for(int i=0;i<n;i++) Tmp[i] = A[i];
		for(int i=0;i<n;i++) A[i] = B[i];
		for(int i=0;i<n;i++) B[i] = Tmp[i];
		return;
	}

	// A[all] == B[all] ? true : false
	public Boolean isSameArray(double A[], double B[]){
		if(A.length != B.length) return false;
		for(int i=0;i<A.length;i++) if(A[i] != B[i]) return false;
		return true;
	}

	// A[all][all] == B[all][all] ? true : false
	public Boolean isSameTable(double A[][], double B[][]){
		if(A.length != B.length || A[0].length != B[0].length) return false;
		for(int i=0;i<A.length;i++) for(int j=0;j<A[0].length;j++) if(A[i][j] != B[i][j]) return false;
		return true;
	}

}