import argparse
import random
import string
import datetime
import os
import csv
import hashlib

#######################################
#DEFAULT EXPERIMENT SETTINGS
INPUT_DIR = "/Users/guoxiwang/Workspace/wisec_2019/data/original_csv/"
OUTPUT_DIR = "."
TTL_SEC = 60*60 #in seconds
EXPERIMENT_ID = "TEST"

TIMESTAMP_FORMAT_STR = "%Y-%m-%d %H:%M:%S.%f"
#timestamp string example: "2018-12-26 00:00:01.675063"
#######################################


#######################################
#GLOBAL
dev_id_to_device_dic = {}
#######################################

#######################################
#CONST
ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
#######################################

class ConnectivityEvent(object):
    def __init__(self, csv_row=None):
        self.original_dev_id = csv_row["client_id"]
        self.ap_id = csv_row["ap_id"]
        self.ap_mac = csv_row["ap_mac"]
        self.timestamp_str = csv_row["time"]
        self.timestamp = datetime.datetime.strptime(self.timestamp_str, TIMESTAMP_FORMAT_STR)
        self.type = csv_row["type"]

class Device(object):
    def __init__(self, first_conn_event=None, changing_time_delta=None):
        self.original_id = first_conn_event.original_dev_id
        self.last_change_timestamp = first_conn_event.timestamp
        self.changing_time_delta = changing_time_delta

        self.ttl_ids = []
        self.current_salt = None

        self._generate_new_salt()
    
    def _generate_new_salt(self):
        self.current_salt = ''.join(random.choice(ALPHABET) for i in range(16))

    def get_current_ttl_id(self):
        return hashlib.sha256((self.original_id + "||" + self.current_salt).encode('ascii')).hexdigest()

    def get_dev_id_for_event(self, conn_event):
        dev_id = None
        if (conn_event.timestamp - self.last_change_timestamp) > self.changing_time_delta:
            self._generate_new_salt()
            dev_id = self.get_current_ttl_id()
            self.ttl_ids.append(dev_id)
            self.last_change_timestamp = conn_event.timestamp
        else:
            dev_id = self.get_current_ttl_id()
            
        return dev_id

def get_file_list(input_dir):
    file_list = os.listdir(input_dir)
    i = 0
    size_of_list = len(file_list)
    while i < size_of_list:
        f = file_list[i]
        if not f.endswith(".csv"):
            file_list.pop(i)
            size_of_list = size_of_list - 1
        else:
            file_list[i] = input_dir + file_list[i]
            i = i + 1
    file_list.sort()
    for f in file_list:
        f = input_dir + f
    return file_list

def generate_ttl_processed_data(file_list, ttl, experiment_id, output_dir):
    ttl_time_delta = datetime.timedelta(seconds=ttl)

    output_file = output_dir + "/" + experiment_id + "_processed_data.csv"
    with open(output_file,"w") as fout:
        fout.write("time,ap_id,ap_mac,ttl_client_id,type\n")
        for csv_file in file_list:
            print("Processing: "+csv_file)
            with open(csv_file, 'r') as fin:
                csv_reader = csv.DictReader(fin)
                for row in csv_reader:
                    # Per line processing
                    conn_event = ConnectivityEvent(row)
                    if conn_event.original_dev_id not in dev_id_to_device_dic:
                        new_dev = Device(conn_event, ttl_time_delta)
                        dev_id_to_device_dic[conn_event.original_dev_id] = new_dev
                        ttl_dev_id = new_dev.get_current_ttl_id()
                    else:
                        dev = dev_id_to_device_dic[conn_event.original_dev_id]
                        ttl_dev_id = dev.get_dev_id_for_event(conn_event)
                    
                    new_line = conn_event.timestamp_str + ',' + conn_event.ap_id + ',' + conn_event.ap_mac + ','\
                               + ttl_dev_id + ','\
                               + conn_event.type + '\n'

                    fout.write(new_line)

def generate_ttl_meta_data(experiment_id, output_dir):
    output_meta_file = output_dir + "/" + experiment_id + "_meta_data.csv"

    with open(output_meta_file,"w") as fout:
        for original_dev_id in dev_id_to_device_dic:
            line = original_dev_id
            dev = dev_id_to_device_dic[original_dev_id]
            for ttl_id in dev.ttl_ids:
                line += ',' 
                line += ttl_id
            line += '\n'
            
            fout.write(line)

def Main():
    ap = argparse.ArgumentParser()
    ap.add_argument("-d", "--input_directory", help="directory of data csv files", required=False)
    ap.add_argument("-t", "--interval", help="TTL (interval to change the salt) in seconds", required=False)
    ap.add_argument("-i", "--id", help="Experiment ID", required=False)
    ap.add_argument("-o", "--output_dir", help="Experiment ID", required=False)
    args = vars(ap.parse_args())

    # Read from input csv and generate one big csv which device ids are changed periodically
    if args["input_directory"] is None:
        input_dir = INPUT_DIR
    else:
        input_dir = args["input_directory"]

    if args["interval"] is None:
        ttl = TTL_SEC
    else:
        ttl = args["interval"]

    if args["id"] is None:
        experiment_id = EXPERIMENT_ID
    else:
        experiment_id = args["id"]

    if args["output_dir"] is None:
        output_dir = OUTPUT_DIR
    else:
        output_dir = args["output_dir"]

    file_list = get_file_list(input_dir)

    generate_ttl_processed_data(file_list, ttl, experiment_id, output_dir)

    generate_ttl_meta_data(experiment_id, output_dir)

if __name__ == '__main__':
    Main()