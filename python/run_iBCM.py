from iBCM import ConstraintMining, Constraint


def load_dataset(trace_file, label_file):

    traces = []
    label_list = []
    labels = set()
    constraints_per_label = {}
    for trace, label in zip(trace_file, label_file):
        traces.append(trace)
        label_list.append(label.replace('\n',''))
        labels.add(label.replace('\n',''))
    
    traces_per_label = {}
    for la, current_label in enumerate(labels):
        print('\nCurrent label: ', current_label)
        final_traces = []
        activity_count = {}
        for label, trace in zip(label_list, traces):
            if label == current_label:
                trace = trace.replace('\n', '')
                trace = trace.replace(' -1 -2', '')
#                print(trace)
                acts = trace.split(' -1 ')
                for act in acts:
                    if act not in activity_count.keys():
                        activity_count[act] = 0
                    activity_count[act] += 1
                final_traces.append(acts)
        traces_per_label[label] = final_traces
        		
        non_redundant_activities = set()
#        print(activity_count)
        for act, count in activity_count.items():
            if count >= len(final_traces) * min_sup:
                non_redundant_activities.add(act)
        print('#non-redundant activities: ',len(non_redundant_activities))
        non_redundant_activities = sorted(non_redundant_activities)

        constraints_for_label = mine_constraints(final_traces, non_redundant_activities, current_label, no_win)
#        if current_label == '6':
#            for c in constraints_for_label:
#                print(c)        
        constraints_per_label[current_label] = constraints_for_label
    return constraints_per_label


def mine_constraints(traces, non_redundant_activities, label, no_win):
	
    too_short = 0
    constraint_count = {}
    actual_traces = 0
    for t, trace in enumerate(traces):
        if len(trace) >= no_win: 
            actual_traces += 1
            miner = ConstraintMining(trace, label, non_redundant_activities, no_win)
            constraints = miner.FindConstraints()
#            print('Constraints for trace ', t, ' ', constraints)
            for constraint in constraints:
                if constraint not in constraint_count.keys():
                    constraint_count[constraint] = 0
                constraint_count[constraint] += 1
        else:
            too_short += 1
    print('Too short: ', too_short)
    print('#constraints prior removal: ', len(constraint_count))


    to_remove = set()
    for constraint, count in constraint_count.items():
        if count < (actual_traces * min_sup):
            to_remove.add(constraint)
    for tr in to_remove:
        del constraint_count[tr]
#    print('#constraints post removal: ', len(constraint_count))
#    if label == '6':
#        for constraint in constraint_count:
#            print(constraint)
    return constraint_count.keys()


def reduce_feature_space(constraints):
    print('Begin size: ', len(constraints))

    toRemove = set()
    
    lookAt = set()
    lookOut = set()
    for c in constraints:
        if 'succession' in c.name:
            lookAt.add(c)
        if 'response' in c.name or 'precedence' in c.name or 'succession' in c.name or 'co_existence' in c.name:
            lookOut.add(c)

    for c in lookAt:
        for c2 in lookOut:
            if c.w==c2.w and c.a==c2.a and c.b==c2.b:
                if c.name == 'succession' and (c2.name=='response' or c2.name=='precedence'):
                    toRemove.add(c2)
                if c.name == 'alternate_succession' and 'chain' not in c.name and ('response' in c2.name or 'precedence' in c2.name or c2.name=='succession'):
                    toRemove.add(c2)
                if c.name == 'chain_succession' and ('response' in c2.name or 'precedence' in c2.name or c2.name=='succession' or c2.name=='alternate_succession'):
                    toRemove.add(c2)
            if c.w==c2.w and ((c.a==c2.a and c.b==c2.b) or (c.b==c2.a and c.a==c2.b)):   
                 if c.name=='succession' and c2.name=='co_existence':
                     toRemove.add(c2)
    print('Remove size: ', len(toRemove))
    constraints = constraints.difference(toRemove)
    toRemove = set()
		
    lookAt = set()
    for c in constraints:
        if 'response' in c.name or 'precedence' in c.name:
            lookAt.add(c)
		
    for c in lookAt:
        if c.name == 'chain_response':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and c.b==c2.b:
                    if c2.name == 'response' or c2.name == 'alternate_response':
                        toRemove.add(c2)
        if c.name == 'chain_precedence':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and c.b==c2.b:
                    if c2.name == 'precedence' or c2.name == 'alternate_precedence':
                        toRemove.add(c2)
        if c.name == 'alternate_response':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and c.b==c2.b:
                    if c2.name == 'response':
                        toRemove.add(c2)
        if c.name == 'alternate_precedence':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and c.b==c2.b:
                    if c2.name == 'precedence':
                        toRemove.add(c2)                        
    print('Remove size: ', len(toRemove))
    lookAt = set()
    for c in constraints:
        if 'exactly' in c.name or 'existence' in c.name:
            lookAt.add(c)
		
    for c in lookAt:
        if c.name == 'existence3':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and (c2.name=='existence2' or c2.name=='existence'):
                    toRemove.add(c2)
        if c.name == 'existence2':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and c2.name=='existence':
                    toRemove.add(c2)
        if c.name == 'exactly2':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and (c2.name=='existence' or c2.name=='exactly' or c2.name=='existence2'):
                    toRemove.add(c2)                    
        if c.name == 'exactly2':
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and (c2.name=='existence' or c2.name=='exactly' or c2.name=='existence2'):
                    toRemove.add(c2)   
        if ('existence' in c.name or 'exactly' in c.name) and 'co_existence' not in c.name:
            for c2 in lookAt:
                if c.w==c2.w and c.a==c2.a and ('existence' in c2.name or 'exactly' in c2.name) and 'co_existence' not in c2.name:
                    for c3 in constraints:
                        if c3.name=='co_existence' and c2.w==c3.w and ((c.a==c3.a and c2.b==c3.b) or (c.b==c3.a and c2.a==c3.b)):
                            toRemove.add(c3)
    print('Remove size: ', len(toRemove))
    constraints = constraints.difference(toRemove)
    print('End size: ', len(constraints))
    return constraints	

	
    
min_sup = 0.1
no_win = 5
constraints = set()

name = 'context'
traces = open(name+'.dat', 'r')
labels = open(name+'.lab', 'r')

constraints_per_label = load_dataset(traces, labels)

print('\n\n**********************\nFinal stats: ')

all_constraints = set()
for label, constraints in constraints_per_label.items():
    print('Label ', label, ' has ', len(constraints), ' constraints')
    all_constraints = all_constraints.union(set(constraints))
print('\nTotal # constraints: ', len(all_constraints))

    
overlapping = set()
for constraint in all_constraints:
    overlap = True
    for label, constraints in constraints_per_label.items():
        if constraint not in constraints:
            overlap = False
            break
    if overlap:
        overlapping.add(constraint)
print('Overlapping constraints: ', len(overlapping))
all_constraints = all_constraints.difference(overlapping)
print('Total # constraints w/o overlap: ', len(all_constraints))
final_constraints = reduce_feature_space(all_constraints)
print('Final total # constraints: ', len(final_constraints))
