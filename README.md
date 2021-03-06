# iBCM (interesting Behavioral Constraint Miner)

This document supports the Java and Python implementation of the sequence mining/classifcation algorithm iBCM.

## About
Sequence classification deals with the task of finding discriminative and concise sequential patterns. 
To this purpose, many techniques have been proposed, which mainly resort to the use of partial orders to capture the underlying sequences in a database according to the labels. Partial orders, however, pose many limitations, especially on expressiveness, i.e. the aptitude towards capturing certain behavior, and on conciseness, i.e. doing so in a compact and informative way. These limitations can be addressed by using a better representation. 

In this paper we present the interesting Behavioral Constraint Miner (iBCM), a sequence classification technique that discovers patterns using behavioral constraint templates. The templates comprise a variety of constraints and can express patterns ranging from simple occurrence, to looping and position-based behavior over a sequence. Furthermore, iBCM also captures negative constraints, i.e. absence of particular behavior. The constraints can be discovered by using simple string operations in an efficient way. Finally, deriving the constraints with a window-based approach allows to pinpoint where the constraints hold in a string, and to detect whether patterns are subject to concept drift. Through empirical evaluation, it is shown that iBCM is better capable of classifying sequences more accurately and concisely in a scalable manner.

## Publication
iBCM was presented at the <a href="https://link.springer.com/chapter/10.1007/978-3-319-71246-8_2">European Conference on Machine Learning & Principles and Practice of Knowledge Discovery in Databases (ECML PKDD 2017)</a>, and later published as an extended version in <a href="https://ieeexplore.ieee.org/document/8633396">IEEE Transactions on Knowledge and Data Discovery</a>.

## Implementation
There are two ways of using iBCM: either using the full Python implementation, or using Python to create training/test sets, mine the sequences with Java, and perform classification with Python again. Java can leverage multithreading and an overall much more efficient code base, as well as mine sequences with [ISM](https://github.com/mast-group/sequence-mining), [MiSeRe](http://www.misere.co.nf/), [PrefixSPAN](http://hanj.cs.illinois.edu/pdf/span01.pdf), [SPADE](https://link.springer.com/article/10.1023/A:1007652502315), and [BIDE](https://ieeexplore.ieee.org/document/1319986) for experimental evaluation.

### Python
To use iBCM only with Python, store the datasets in an adjacent datasets folder and run [iBCM_Python.py](./python/iBCM_Python.py). The implementation also uses [run_iBCM.py](./python/run_iBCM.py) and [iBCM.py](./python/iBCM.py).

### Python + Java
To create cross validation training/test files from datasets in an adjacent ```./datasets/``` folder, use [create_training_test_datasets.py](./python/create_training_test_datasets.py). This will store the required files under ```./datasets/training-test-data/``` as well as separate files for MiSeRe under ```./datasets/training-test-data/MiSeRe_data/```.

Then you can run the provided Java code with the corresponding number of folds. The results are stored in the same folders as the folds' original files. Next, you can classify using [run_classification.py](./python/run_classification.py).

You can use the JAR file as follows:

```java -jar iBCM.jar -d auslan2,context -t iBCM,MiSeRe -s 0.5 -n 2```

<h3>The following arguments are used:</h3>
<ul><li>-h for help</li>	
<li>-d for datasets (e.g. Unix,auslan2)</li>
<li>-s for support values of support-based approaches (e.g. 0.1,0.2,0.3)</li>
<li>-t for the techniques that need to be used (possible values: iBCM,MiSeRe,SPADE,PrefixSPAN,BIDE,ISM)</li>
<li>-w for the window parameter of iBCM</li>
<li>-n for the number of seconds for MiSeRe</li>
<li>-im for the maximum number of iterations for ISM</li>
<li>-is for the number of steps for ISM</li>
 <li>-f for the number of folds</li>
  
</ul>
Datsets are included in the datasets folder, which should be used in the home directory of the .jar file.
Values for datasets: "Unix", "auslan2", "aslbu", "pioneer", "context", and "reuters".
If you wish to test your own datasets, this can be done using the SPMF format, by including one .dat file with the sequence and 1 file containing the label on the same line as the sequence in file .lab.
