// Code generated by go-bindata. (@generated) DO NOT EDIT.

// Package schema generated by go-bindata.// sources:
// callgraph.avsc
package schema

import (
	"bytes"
	"compress/gzip"
	"fmt"
	"io"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"time"
)

func bindataRead(data []byte, name string) ([]byte, error) {
	gz, err := gzip.NewReader(bytes.NewBuffer(data))
	if err != nil {
		return nil, fmt.Errorf("read %q: %v", name, err)
	}

	var buf bytes.Buffer
	_, err = io.Copy(&buf, gz)
	clErr := gz.Close()

	if err != nil {
		return nil, fmt.Errorf("read %q: %v", name, err)
	}
	if clErr != nil {
		return nil, err
	}

	return buf.Bytes(), nil
}

type asset struct {
	bytes []byte
	info  os.FileInfo
}

type bindataFileInfo struct {
	name    string
	size    int64
	mode    os.FileMode
	modTime time.Time
}

// Name return file name
func (fi bindataFileInfo) Name() string {
	return fi.name
}

// Size return file size
func (fi bindataFileInfo) Size() int64 {
	return fi.size
}

// Mode return file mode
func (fi bindataFileInfo) Mode() os.FileMode {
	return fi.mode
}

// ModTime return file modify time
func (fi bindataFileInfo) ModTime() time.Time {
	return fi.modTime
}

// IsDir return file whether a directory
func (fi bindataFileInfo) IsDir() bool {
	return fi.mode&os.ModeDir != 0
}

// Sys return file is sys mode
func (fi bindataFileInfo) Sys() interface{} {
	return nil
}

var _callgraphAvsc = []byte("\x1f\x8b\x08\x00\x00\x00\x00\x00\x00\xff\xdc\x92\xb1\x6e\x84\x30\x0c\x86\xf7\x7b\x0a\xcb\x33\xba\x07\x60\xed\x0b\x74\xaf\x6e\xb0\x88\x7b\x44\x85\x04\xd9\xee\x70\x42\xf7\xee\x15\x70\xb9\x12\xc8\x50\x71\x5b\x99\x92\x3f\xff\xef\x0f\x27\x1e\x4f\x00\x00\x18\xa8\x67\xac\x01\xdf\x49\x38\x18\x56\x8b\x6a\xb7\x81\xb1\x46\xe1\x26\x8a\x4b\xe2\x64\xd5\x81\x1a\x46\xa8\x01\xcd\x9f\x5b\x92\xc0\xaa\x67\x1f\x93\xe5\xd3\x73\xe7\x14\xeb\x8f\x79\x3b\x7d\xe3\x73\xb5\xc2\x61\x88\x8e\xf5\x11\x7a\x9e\x2d\xd0\x3c\xb0\x3a\x00\x24\x11\xba\x61\x05\xb0\xb7\x78\xe3\x5e\x4b\xe1\x35\xf5\xad\xf5\x9d\xdb\x50\x37\x90\xbc\xe5\x9d\x69\xdf\xdf\xf6\x1b\x17\xd8\x74\x43\x3d\x5b\x1b\x1d\x56\x8f\xda\x93\xa4\x26\x3e\x5c\xf1\x5e\x2e\xbf\xc9\x0f\xd4\x7c\xd1\x95\x8f\x17\xf0\x39\xdc\x07\xfb\x33\x59\xa8\xd7\xe3\xe0\xa6\x23\x7d\x21\x3e\xa7\x4a\xe9\x62\xf8\xb2\x53\x73\xdf\xef\x6e\x45\x2f\x8f\xa5\x70\x47\xe6\x63\xf8\xf7\xa3\xa9\xf1\x5b\x1a\x3e\x38\x1d\xc6\x6a\xd9\xeb\x8e\xe9\x0f\xd3\x2d\xa4\x9e\x97\xa2\x2f\x3f\x5b\x5a\xcc\xca\x05\x4e\xf7\xd3\x4f\x00\x00\x00\xff\xff\xdf\x39\x10\x66\xbd\x04\x00\x00")

func callgraphAvscBytes() ([]byte, error) {
	return bindataRead(
		_callgraphAvsc,
		"callgraph.avsc",
	)
}

func callgraphAvsc() (*asset, error) {
	bytes, err := callgraphAvscBytes()
	if err != nil {
		return nil, err
	}

	info := bindataFileInfo{name: "callgraph.avsc", size: 1213, mode: os.FileMode(420), modTime: time.Unix(1616408001, 0)}
	a := &asset{bytes: bytes, info: info}
	return a, nil
}

// Asset loads and returns the asset for the given name.
// It returns an error if the asset could not be found or
// could not be loaded.
func Asset(name string) ([]byte, error) {
	canonicalName := strings.Replace(name, "\\", "/", -1)
	if f, ok := _bindata[canonicalName]; ok {
		a, err := f()
		if err != nil {
			return nil, fmt.Errorf("Asset %s can't read by error: %v", name, err)
		}
		return a.bytes, nil
	}
	return nil, fmt.Errorf("Asset %s not found", name)
}

// MustAsset is like Asset but panics when Asset would return an error.
// It simplifies safe initialization of global variables.
func MustAsset(name string) []byte {
	a, err := Asset(name)
	if err != nil {
		panic("asset: Asset(" + name + "): " + err.Error())
	}

	return a
}

// AssetInfo loads and returns the asset info for the given name.
// It returns an error if the asset could not be found or
// could not be loaded.
func AssetInfo(name string) (os.FileInfo, error) {
	canonicalName := strings.Replace(name, "\\", "/", -1)
	if f, ok := _bindata[canonicalName]; ok {
		a, err := f()
		if err != nil {
			return nil, fmt.Errorf("AssetInfo %s can't read by error: %v", name, err)
		}
		return a.info, nil
	}
	return nil, fmt.Errorf("AssetInfo %s not found", name)
}

// AssetNames returns the names of the assets.
func AssetNames() []string {
	names := make([]string, 0, len(_bindata))
	for name := range _bindata {
		names = append(names, name)
	}
	return names
}

// _bindata is a table, holding each asset generator, mapped to its name.
var _bindata = map[string]func() (*asset, error){
	"callgraph.avsc": callgraphAvsc,
}

// AssetDir returns the file names below a certain
// directory embedded in the file by go-bindata.
// For example if you run go-bindata on data/... and data contains the
// following hierarchy:
//     data/
//       foo.txt
//       img/
//         a.png
//         b.png
// then AssetDir("data") would return []string{"foo.txt", "img"}
// AssetDir("data/img") would return []string{"a.png", "b.png"}
// AssetDir("foo.txt") and AssetDir("nonexistent") would return an error
// AssetDir("") will return []string{"data"}.
func AssetDir(name string) ([]string, error) {
	node := _bintree
	if len(name) != 0 {
		canonicalName := strings.Replace(name, "\\", "/", -1)
		pathList := strings.Split(canonicalName, "/")
		for _, p := range pathList {
			node = node.Children[p]
			if node == nil {
				return nil, fmt.Errorf("Asset %s not found", name)
			}
		}
	}
	if node.Func != nil {
		return nil, fmt.Errorf("Asset %s not found", name)
	}
	rv := make([]string, 0, len(node.Children))
	for childName := range node.Children {
		rv = append(rv, childName)
	}
	return rv, nil
}

type bintree struct {
	Func     func() (*asset, error)
	Children map[string]*bintree
}

var _bintree = &bintree{nil, map[string]*bintree{
	"callgraph.avsc": &bintree{callgraphAvsc, map[string]*bintree{}},
}}

// RestoreAsset restores an asset under the given directory
func RestoreAsset(dir, name string) error {
	data, err := Asset(name)
	if err != nil {
		return err
	}
	info, err := AssetInfo(name)
	if err != nil {
		return err
	}
	err = os.MkdirAll(_filePath(dir, filepath.Dir(name)), os.FileMode(0755))
	if err != nil {
		return err
	}
	err = ioutil.WriteFile(_filePath(dir, name), data, info.Mode())
	if err != nil {
		return err
	}
	err = os.Chtimes(_filePath(dir, name), info.ModTime(), info.ModTime())
	if err != nil {
		return err
	}
	return nil
}

// RestoreAssets restores an asset under the given directory recursively
func RestoreAssets(dir, name string) error {
	children, err := AssetDir(name)
	// File
	if err != nil {
		return RestoreAsset(dir, name)
	}
	// Dir
	for _, child := range children {
		err = RestoreAssets(dir, filepath.Join(name, child))
		if err != nil {
			return err
		}
	}
	return nil
}

func _filePath(dir, name string) string {
	canonicalName := strings.Replace(name, "\\", "/", -1)
	return filepath.Join(append([]string{dir}, strings.Split(canonicalName, "/")...)...)
}
