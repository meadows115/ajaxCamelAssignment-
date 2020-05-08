/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

"use strict";
/**
 * Create customer module
 */
let module = angular.module('CustomerModule', ['ngResource']);
let serviceURI = 'http://localhost:8080/api';


module.factory('getCustomerApi', function ($resource)
{ return $resource(serviceURI + '/customer'); });

//customer 
module.factory('createCustomerApi', function ($resource) { 
    return $resource(serviceURI + '/customer', 
    null, {update: {method: 'POST'}}); });

//jetty using a different port 
module.factory('createCustomerApiJetty', function ($resource) { 
    return $resource( 'http://localhost:9000/api'+ '/customer', 
    null, {update: {method: 'POST'}}); });


//customer controller 
module.controller('CustomerController', function (getCustomerApi, createCustomerApi, createCustomerApiJetty) {
// save 'this' so we can access it from other scopes 
 let ctrl = this;
 
 // get all customers and load them into the 'customers' model
  ctrl.customers = getCustomerApi.query();
  
    this.addCustomer = function (customerToAdd){ 
        createCustomerApiJetty.save({}, customerToAdd, function(){
            ctrl.customers=getCustomerApi.query();
        });
        createCustomerApi.save({},customerToAdd, function(){
            ctrl.customers=getCustomerApi.query();
        });
  };
  
});

