# iBCM (interesting Behavioral Constraint Miner)

<h2>About</h2>
Sequence classification deals with the task of finding discriminative and concise sequential patterns. 
To this purpose, many techniques have been proposed, which mainly resort to the use of partial orders to capture the underlying sequences in a database according to the labels. Partial orders, however, pose many limitations, especially on expressiveness, i.e. the aptitude towards capturing certain behavior, and on conciseness, i.e. doing so in a compact and informative way. These limitations can be addressed by using a better representation. 

In this paper we present the interesting Behavioral Constraint Miner (iBCM), a sequence classification technique that discovers patterns using behavioral constraint templates. The templates comprise a variety of constraints and can express patterns ranging from simple occurrence, to looping and position-based behavior over a sequence. Furthermore, iBCM also captures negative constraints, i.e. absence of particular behavior. The constraints can be discovered by using simple string operations in an efficient way. Finally, deriving the constraints with a window-based approach allows to pinpoint where the constraints hold in a string, and to detect whether patterns are subject to concept drift. Through empirical evaluation, it is shown that iBCM is better capable of classifying sequences more accurately and concisely in a scalable manner.

<h2>Publication</h2>
iBCM was presented at the <a href="https://link.springer.com/chapter/10.1007/978-3-319-71246-8_2">European Conference on Machine Learning & Principles and Practice of Knowledge Discovery in Databases (ECML PKDD 2017)</a>, with an extended version published in <a href="https://ieeexplore.ieee.org/document/8633396">IEEE Transactions on Knowledge and Data Discovery</a>.

<h2>Implementation</h2>
You can use the implementation as follows:

java -cp iBCM.jar be.kuleuven.liris.sequencemining StartiBCM_SPMF

<h3>The following arguments are used:</h3>
<ul><li>-h for help</li>	
<li>-d for datasets (see below)</li>
<li>-s for support values of support-based approaches (e.g. 0.1,0.2,0.3)</li>
<li>-o for the output directory</li>
<li>-w for the window parameter of iBCM</li>
</ul>
Datsets are included in <a href="./logs.rar">this file</a>, and should be used in the home directory of the .jar file.
Values for datasets: "Unix", "auslan2", "aslbu", "pioneer", "context", and "news".
If you wish to test your own datasets, this can be done for all techniques (all but MiSeRe and SCIP) using the SPMF format, by including 1 .dat file with the sequence in logs/SPMF_data/ and 1 file containing the label on the same line as the sequence in file .lab.

The results are stored in a csv file (location can be specified with option -o).
