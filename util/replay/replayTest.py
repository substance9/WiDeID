import os
import time

#HISTORY_DATA_DIR = './history_data/'  
HISTORY_DATA_DIR ='/Users/linyiming/Documents/research/Occupancy/data/'
HISTORY_DATA_FILE = "DBH_AP_SNMP.2018-10-22"
HISTORY_DATA_FILE1 = "DBH_AP_SNMP.2018-10-23"
HISTORY_DATA_FILE2 = "DBH_AP_SNMP.2018-10-24"

DESTINATION_PIPE_LOCATION = "../pipe2"
DESTINATION_TESTING = "output"

EVENT_INTERVAL_SEC = 0.001 #adjust the number of events per second, 0.01 -> 100 events per second

def replay(origin_data_file_d, destination_file_d):
    for line in origin_data_file_d:
        if len(line) > 2:
            #line_sec = line.split("|")
            #replay_line = line_sec[-1].strip()
            line.strip()
            destination_file_d.write(line)
            print("---Sending Event: " + line)
            destination_file_d.write('\n')
            destination_file_d.flush()
        time.sleep(EVENT_INTERVAL_SEC)

def main():
    with open(DESTINATION_PIPE_LOCATION, 'w') as destination_file_d:
        for filename in os.listdir(HISTORY_DATA_DIR):
            #if filename.startswith(LOG_FILENAME_PREFIX):
            if filename == HISTORY_DATA_FILE:
                print("Replaying:" + str(os.path.join(HISTORY_DATA_DIR, filename)))
                origin_data_file = os.path.join(HISTORY_DATA_DIR, filename)
                with open(origin_data_file, 'r') as origin_file_d:
                    replay(origin_file_d, destination_file_d)

                print("Done")
            
            else:
                continue


if __name__ == '__main__':
   main()
