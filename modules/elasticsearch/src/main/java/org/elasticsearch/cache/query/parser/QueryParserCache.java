begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cache.query.parser
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|query
operator|.
name|parser
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queryParser
operator|.
name|QueryParserSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_interface
DECL|interface|QueryParserCache
specifier|public
interface|interface
name|QueryParserCache
block|{
DECL|method|get
name|Query
name|get
parameter_list|(
name|QueryParserSettings
name|queryString
parameter_list|)
function_decl|;
DECL|method|put
name|void
name|put
parameter_list|(
name|QueryParserSettings
name|queryString
parameter_list|,
name|Query
name|query
parameter_list|)
function_decl|;
DECL|method|clear
name|void
name|clear
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

