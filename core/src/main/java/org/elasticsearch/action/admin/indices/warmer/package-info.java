begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_comment
comment|/**  * Index / Search Warmer Administrative Actions  *<p>  *     Index warming allows to run registered search requests to warm up the index before it is available for search.  *     With the near real time aspect of search, cold data (segments) will be warmed up before they become available for  *     search. This includes things such as the query cache, filesystem cache, and loading field data for fields.  *</p>  *  * @see the reference guide for more detailed information about the Indices / Search Warmer  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.warmer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|warmer
package|;
end_package

end_unit

