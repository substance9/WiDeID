import os
import time

HISTORY_DATA_DIR = './history_data/'
HISTORY_DATA_FILE = "snmptrapd.log"

DESTINATION_PIPE_LOCATION = "./history_data/test.log"

EVENT_INTERVAL_SEC = 0.5 #adjust the number of events per second, 0.01 -> 100 events per second

def replay(origin_data_file_d, destination_file_d):
    for line in origin_data_file_d:
        if len(line) > 2:
#             line_sec = line.split("|")
#             replay_line = line_sec[-1].strip()
            replay_line = line
            destination_file_d.write(replay_line)
            print("---Sending Event: " + replay_line)
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
