begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.subphase.highlight
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|highlight
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
name|search
operator|.
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|FieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|FetchSubPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_class
DECL|class|HighlighterContext
specifier|public
class|class
name|HighlighterContext
block|{
DECL|field|fieldName
specifier|public
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|field
specifier|public
specifier|final
name|SearchContextHighlight
operator|.
name|Field
name|field
decl_stmt|;
DECL|field|mapper
specifier|public
specifier|final
name|FieldMapper
name|mapper
decl_stmt|;
DECL|field|context
specifier|public
specifier|final
name|SearchContext
name|context
decl_stmt|;
DECL|field|hitContext
specifier|public
specifier|final
name|FetchSubPhase
operator|.
name|HitContext
name|hitContext
decl_stmt|;
DECL|field|query
specifier|public
specifier|final
name|Query
name|query
decl_stmt|;
DECL|method|HighlighterContext
specifier|public
name|HighlighterContext
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|SearchContextHighlight
operator|.
name|Field
name|field
parameter_list|,
name|FieldMapper
name|mapper
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|FetchSubPhase
operator|.
name|HitContext
name|hitContext
parameter_list|,
name|Query
name|query
parameter_list|)
block|{
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|mapper
operator|=
name|mapper
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|hitContext
operator|=
name|hitContext
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
block|}
block|}
end_class

end_unit

