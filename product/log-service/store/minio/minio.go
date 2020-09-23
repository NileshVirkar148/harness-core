// Package minio provides a log storage driver backed by
// Minio or a Minio-compatible storage system.
package minio

import (
	"context"
	"io"
	"path"
	"strings"
	"time"

	"github.com/wings-software/portal/product/log-service/store"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
)

var _ store.Store = (*Store)(nil)

// Store provides a log storage driver backed by Minio or a
// Minio-compatible storage system.
type Store struct {
	bucket  string
	prefix  string
	session *session.Session
}

// NewEnv returns a new S3 log store from the environment.
func NewEnv(bucket, prefix, endpoint string, pathStyle bool) *Store {
	disableSSL := false

	if endpoint != "" {
		disableSSL = !strings.HasPrefix(endpoint, "https://")
	}

	return &Store{
		bucket: bucket,
		prefix: prefix,
		session: session.Must(
			session.NewSession(&aws.Config{
				Endpoint:         aws.String(endpoint),
				DisableSSL:       aws.Bool(disableSSL),
				S3ForcePathStyle: aws.Bool(pathStyle),
			}),
		),
	}
}

// New returns a new S3 log store.
func New(session *session.Session, bucket, prefix string) *Store {
	return &Store{
		bucket:  bucket,
		prefix:  prefix,
		session: session,
	}
}

// Download downloads a log stream from the Minio datastore.
func (s *Store) Download(ctx context.Context, key string) (io.ReadCloser, error) {
	svc := s3.New(s.session)
	keyWithPrefix := path.Join("/", s.prefix, key)
	out, err := svc.GetObject(&s3.GetObjectInput{
		Bucket: aws.String(s.bucket),
		Key:    aws.String(keyWithPrefix),
	})
	if err != nil {
		return nil, err
	}
	return out.Body, nil
}

// DownloadLink creates a pre-signed link that can be used to
// download the logs to the Minio datastore.
func (s *Store) DownloadLink(ctx context.Context, key string, expire time.Duration) (string, error) {
	svc := s3.New(s.session)
	keyWithPrefix := path.Join("/", s.prefix, key)
	req, _ := svc.GetObjectRequest(&s3.GetObjectInput{
		Bucket: aws.String(s.bucket),
		Key:    aws.String(keyWithPrefix),
	})
	return req.Presign(expire)
}

// Upload uploads the log stream from Reader r to the
// Minio datastore.
func (s *Store) Upload(ctx context.Context, key string, r io.Reader) error {
	uploader := s3manager.NewUploader(s.session)
	keyWithPrefix := path.Join("/", s.prefix, key)
	input := &s3manager.UploadInput{
		ACL:    aws.String("private"),
		Bucket: aws.String(s.bucket),
		Key:    aws.String(keyWithPrefix),
		Body:   r,
	}
	_, err := uploader.Upload(input)
	return err
}

// UploadLink creates a pre-signed link that can be used to
// upload the logs to the Minio datastore.
func (s *Store) UploadLink(ctx context.Context, key string, expire time.Duration) (string, error) {
	svc := s3.New(s.session)
	keyWithPrefix := path.Join("/", s.prefix, key)
	req, _ := svc.PutObjectRequest(&s3.PutObjectInput{
		Bucket: aws.String(s.bucket),
		Key:    aws.String(keyWithPrefix),
	})
	return req.Presign(expire)
}

// Delete purges the log stream from the Minio datastore.
func (s *Store) Delete(ctx context.Context, key string) error {
	svc := s3.New(s.session)
	keyWithPrefix := path.Join("/", s.prefix, key)
	_, err := svc.DeleteObject(&s3.DeleteObjectInput{
		Bucket: aws.String(s.bucket),
		Key:    aws.String(keyWithPrefix),
	})
	return err
}
