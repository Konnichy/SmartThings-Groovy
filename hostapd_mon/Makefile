SRCS=hostapd_mon.c wpa_ctrl.c
BIN=hostapd_mon

CFLAGS=-Wall

INSTALL_PATH=/usr/local/bin
SYSTEMD_SERVICE=$(BIN:=.service)
SYSTEMD_PATH=/lib/systemd/system/

all: $(BIN)

$(BIN): $(SRCS)

install:
	cp $(BIN) $(INSTALL_PATH)
	cp $(SYSTEMD_SERVICE) $(SYSTEMD_PATH)
	systemctl enable $(SYSTEMD_SERVICE)
	systemctl start $(SYSTEMD_SERVICE)

clean:
	rm $(BIN)
