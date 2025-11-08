#!/usr/bin/env python3
from http.server import HTTPServer, BaseHTTPRequestHandler
import cgi

class UploadHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != '/Upload':
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'Not Found')
            return

        ctype, pdict = cgi.parse_header(self.headers.get('content-type', ''))
        length = int(self.headers.get('content-length', 0))

        try:
            if ctype == 'multipart/form-data':
                fs = cgi.FieldStorage(fp=self.rfile, headers=self.headers, environ={'REQUEST_METHOD':'POST'}, keep_blank_values=True)
                if 'image' in fs:
                    fileitem = fs['image']
                    filename = fileitem.filename or 'uploaded.bin'
                    data = fileitem.file.read()
                    with open(filename, 'wb') as f:
                        f.write(data)
                    print(f"Saved uploaded file: {filename} ({len(data)} bytes)")
                # optional: save other form fields
                name = fs.getvalue('name') if 'name' in fs else None
                ts = fs.getvalue('timestamp') if 'timestamp' in fs else None
                print('name=', name, 'timestamp=', ts)
                self.send_response(200)
                self.end_headers()
                self.wfile.write(b'Upload Successful!')
            else:
                # read raw body
                data = self.rfile.read(length)
                with open('upload_body.bin', 'wb') as f:
                    f.write(data)
                print(f"Saved raw upload body ({len(data)} bytes)")
                self.send_response(200)
                self.end_headers()
                self.wfile.write(b'Upload Received')
        except Exception as e:
            print('Error handling upload:', e)
            self.send_response(500)
            self.end_headers()
            self.wfile.write(b'Internal Server Error')

if __name__ == '__main__':
    server_address = ('', 8080)
    httpd = HTTPServer(server_address, UploadHandler)
    print('Starting simple upload server on port 8080...')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print('Shutting down')
        httpd.server_close()

