// Copyright 2020 Harness Inc. All rights reserved.
// Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
// that can be found in the licenses directory at the root of this repository, also available at
// https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.

// Code generated by MockGen. DO NOT EDIT.
// Source: backoff_factory.go

// Package mocks is a generated GoMock package.
package mocks

import (
	backoff "github.com/cenkalti/backoff/v4"
	gomock "github.com/golang/mock/gomock"
	reflect "reflect"
)

// MockBackOffFactory is a mock of BackOffFactory interface.
type MockBackOffFactory struct {
	ctrl     *gomock.Controller
	recorder *MockBackOffFactoryMockRecorder
}

// MockBackOffFactoryMockRecorder is the mock recorder for MockBackOffFactory.
type MockBackOffFactoryMockRecorder struct {
	mock *MockBackOffFactory
}

// NewMockBackOffFactory creates a new mock instance.
func NewMockBackOffFactory(ctrl *gomock.Controller) *MockBackOffFactory {
	mock := &MockBackOffFactory{ctrl: ctrl}
	mock.recorder = &MockBackOffFactoryMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockBackOffFactory) EXPECT() *MockBackOffFactoryMockRecorder {
	return m.recorder
}

// NewBackOff mocks base method.
func (m *MockBackOffFactory) NewBackOff() backoff.BackOff {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "NewBackOff")
	ret0, _ := ret[0].(backoff.BackOff)
	return ret0
}

// NewBackOff indicates an expected call of NewBackOff.
func (mr *MockBackOffFactoryMockRecorder) NewBackOff() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "NewBackOff", reflect.TypeOf((*MockBackOffFactory)(nil).NewBackOff))
}
