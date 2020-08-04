import pandas as pd
import numpy as np
import os
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import accuracy_score, precision_score, roc_auc_score

from sklearn.ensemble import RandomForestClassifier as RF
from sklearn.naive_bayes import MultinomialNB as NB
from sklearn.tree import DecisionTreeClassifier as DT

from run_iBCM import iBCM, iBCM_verify


def run_iBCM(dataset, support): 
    ## Read files
    trace_file = open('./datasets/'+dataset+'.dat', 'r')
    label_file = open('./datasets/'+dataset+'.lab', 'r')
    
    #####################################
    ## Store information and create folds
    traces = []
    label_list = []
    for trace, label in zip(trace_file, label_file):
        traces.append(trace)
        label_list.append(label.replace('\n',''))
    
    label_set = set(label_list)
    no_labels = len(label_set)
    print('#labels:',no_labels)
          
    skf = StratifiedKFold(no_folds)
    
    
    ##########################
    ## Apply iBCM on all folds
    fold_train_results = []
    fold_test_results = []
    
    acc_sum = 0
    feat_sum = 0
    auc_sum = 0
    for fold, (train_index, test_index) in enumerate(skf.split(traces, label_list)):
        print('\nFold ', (fold+1), '/', no_folds)
        training_points = []
        test_points = []
        training_labels = []
        test_labels = []
    
        for i in train_index:
            training_points.append(traces[i])
            training_labels.append(label_list[i])
    
        filename_train = dataset + '_training_fold_' + str(fold) + '_support_' 
        filename_train += str(support) + '.csv'
    
        final_constraints = iBCM(filename_train, training_points, training_labels, reduce_feature_space, support, no_win)       
        
        # Label training data
        iBCM_verify(filename_train, training_points, training_labels, final_constraints, no_win)
        fold_train_results.append(pd.read_csv(filename_train))
            
        filename_test = dataset + '_test_fold_' + str(fold) + '_support_' 
        filename_test += str(support) + '.csv'
        
        for i in test_index:
            test_points.append(traces[i])
            test_labels.append(label_list[i])
        
        # Label test data
        iBCM_verify(filename_test, test_points, test_labels, final_constraints, no_win)
        fold_test_results.append(pd.read_csv(filename_test))
                
        os.remove(filename_train)
        os.remove(filename_test)
         

    #######################
    ## Apply classification       
        
    if write_results:
        write_header = False
        
        if not os.path.exists(name_result_file):
            write_header = True
        results = open(name_result_file,'a')
        
        if write_header:
            results.write('dataset,support,classifier,no_folds,reduce,no_features,accuracy,auc\n')
    
                
    for name, classifier in classifiers: 
        print(f'Classifier ', name)
        
        acc_sum = 0
        feat_sum = 0
        auc_sum = 0
        for i in range(0, no_folds):
    #                print('Fold:',i)
            training = fold_train_results[i]
            test = fold_test_results[i]
                    
            y_train = training['label']
            X_train = training.drop(['label'], axis=1)                 
                                
            y_test = test['label']
            X_test = test.drop(['label'], axis=1)
            
            # print('#Features:', len(X_train.columns))
            
            feat_sum += len(X_train.columns)
            try:
                classifier.fit(X_train,y_train)
                predictions = classifier.predict(X_test)
                predictions_prob = classifier.predict_proba(X_test)
                
                acc = accuracy_score(y_test,predictions)
                auc = 0 #roc_auc_score(y_test,predictions_prob[:,1])
                acc_sum += acc
                auc_sum += auc
            except:
                print('No features')
                break
                acc = 0
                acc_sum += 0
                auc_sum += 0     
        
        avg_feat = feat_sum/no_folds
        avg_acc = acc_sum/no_folds
        avg_auc = auc_sum/no_folds
        if write_results:
            results = open(name_result_file,'a')
            results.write(f'{dataset},{support},{name},{no_folds},{reduce_feature_space},{avg_feat},{avg_acc},{avg_auc}\n')
            results.close()
        print('Avg. acc.:', avg_acc)
        print('Avg. AUC.:', avg_auc)
        print('Avg. #features.:', avg_feat)                     
        

### Start program and enter parameters
no_folds = 10
no_win = 1
reduce_feature_space = True

name_result_file = 'results_iBCM_Python.csv'

classifiers = [('Random forest',RF(n_estimators=100))]

np.random.seed(42)

write_results = True

for dataset in ['auslan2','aslbu','context','pioneer','Unix']:
    print('\nDataset:', dataset)
    for support in [0.2,0.4,0.6,0.8]:
        print('\nSupport level:', support)
        run_iBCM(dataset, support)