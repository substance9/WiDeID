import time
import os
import errno


class Buffer(object):
    """
    The goal of this program is to separate the data receving/red and .to avoid "BROKEN PIPE" issue.


    """
    _first_pipe_location = "./pipe1"
    _second_pipe_location = "./pipe2"
    _first_pipe = None
    #_second_pipe = None
    #_second_pipe_fd = os.open("/dev/null",os.O_WRONLY)

    _second_pipe_writable = False

    def __init__(self):
        try:
            self._first_pipe = open(self._first_pipe_location, 'r')
        except Exception as e:
            print e
            print "ERROR: Can\'t open first pipe file"

        self._open_second_pipe()


    def _open_second_pipe(self):
        try:
            self._second_pipe_fd = os.open(self._second_pipe_location, os.O_WRONLY|os.O_NONBLOCK)
        except OSError as e:
            print e
            print "ERROR: Can\'t open second pipe file"
            if e.errno == errno.ENXIO:
                return  
        else:
            self._second_pipe_writable = True



    def run(self):
        while(True):
            try:
                data_line = self._first_pipe.readline()
            except Exception as e:
                print e
                print "ERROR: Can\'t read line from first pipe file"
                time.sleep(1)
                continue
            else:
                if data_line.strip() != '':
                    if self._second_pipe_writable is True:
                        try:
                            os.write(self._second_pipe_fd, data_line)
                        except OSError as e:
			    print "ERROR: Can\'t Write line to second pipe file"
                            print e
                            if e.errno == errno.EPIPE:
                                self._second_pipe_writable = False
                                os.close(self._second_pipe_fd)
                            print "Missed Data: " + data_line
                            continue
                        else:
                            print "Transfer data: " + data_line
                    # if second pipe file is not writable
                    else:
                        print "Missed Data: " + data_line
                        self._open_second_pipe()


if __name__ == '__main__':
    pbuffer = Buffer()
    pbuffer.run()
