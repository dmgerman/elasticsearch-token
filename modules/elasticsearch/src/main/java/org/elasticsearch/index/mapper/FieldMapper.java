begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|analysis
operator|.
name|Analyzer
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
name|document
operator|.
name|Field
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
name|document
operator|.
name|Fieldable
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
name|Filter
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|StringHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Immutable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadSafe
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_interface
annotation|@
name|ThreadSafe
DECL|interface|FieldMapper
specifier|public
interface|interface
name|FieldMapper
parameter_list|<
name|T
parameter_list|>
block|{
annotation|@
name|Immutable
DECL|class|Names
specifier|public
specifier|static
class|class
name|Names
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|indexName
specifier|private
specifier|final
name|String
name|indexName
decl_stmt|;
DECL|field|indexNameClean
specifier|private
specifier|final
name|String
name|indexNameClean
decl_stmt|;
DECL|field|fullName
specifier|private
specifier|final
name|String
name|fullName
decl_stmt|;
DECL|method|Names
specifier|public
name|Names
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|,
name|String
name|indexNameClean
parameter_list|,
name|String
name|fullName
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexName
operator|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexNameClean
operator|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|indexNameClean
argument_list|)
expr_stmt|;
name|this
operator|.
name|fullName
operator|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|fullName
argument_list|)
expr_stmt|;
block|}
comment|/**          * The logical name of the field.          */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**          * The indexed name of the field. This is the name under which we will          * store it in the index.          */
DECL|method|indexName
specifier|public
name|String
name|indexName
parameter_list|()
block|{
return|return
name|indexName
return|;
block|}
comment|/**          * The cleaned index name, before any "path" modifications performed on it.          */
DECL|method|indexNameClean
specifier|public
name|String
name|indexNameClean
parameter_list|()
block|{
return|return
name|indexNameClean
return|;
block|}
comment|/**          * The full name, including dot path.          */
DECL|method|fullName
specifier|public
name|String
name|fullName
parameter_list|()
block|{
return|return
name|fullName
return|;
block|}
block|}
DECL|method|names
name|Names
name|names
parameter_list|()
function_decl|;
DECL|method|index
name|Field
operator|.
name|Index
name|index
parameter_list|()
function_decl|;
DECL|method|indexed
name|boolean
name|indexed
parameter_list|()
function_decl|;
DECL|method|analyzed
name|boolean
name|analyzed
parameter_list|()
function_decl|;
DECL|method|store
name|Field
operator|.
name|Store
name|store
parameter_list|()
function_decl|;
DECL|method|stored
name|boolean
name|stored
parameter_list|()
function_decl|;
DECL|method|termVector
name|Field
operator|.
name|TermVector
name|termVector
parameter_list|()
function_decl|;
DECL|method|boost
name|float
name|boost
parameter_list|()
function_decl|;
DECL|method|omitNorms
name|boolean
name|omitNorms
parameter_list|()
function_decl|;
DECL|method|omitTermFreqAndPositions
name|boolean
name|omitTermFreqAndPositions
parameter_list|()
function_decl|;
comment|/**      * The analyzer that will be used to index the field.      */
DECL|method|indexAnalyzer
name|Analyzer
name|indexAnalyzer
parameter_list|()
function_decl|;
comment|/**      * The analyzer that will be used to search the field.      */
DECL|method|searchAnalyzer
name|Analyzer
name|searchAnalyzer
parameter_list|()
function_decl|;
comment|/**      * Returns the value that will be used as a result for search. Can be only of specific types... .      */
DECL|method|valueForSearch
name|Object
name|valueForSearch
parameter_list|(
name|Fieldable
name|field
parameter_list|)
function_decl|;
comment|/**      * Returns the actual value of the field.      */
DECL|method|value
name|T
name|value
parameter_list|(
name|Fieldable
name|field
parameter_list|)
function_decl|;
comment|/**      * Returns the actual value of the field as string.      */
DECL|method|valueAsString
name|String
name|valueAsString
parameter_list|(
name|Fieldable
name|field
parameter_list|)
function_decl|;
comment|/**      * Returns the indexed value.      */
DECL|method|indexedValue
name|String
name|indexedValue
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
comment|/**      * Returns the indexed value.      */
DECL|method|indexedValue
name|String
name|indexedValue
parameter_list|(
name|T
name|value
parameter_list|)
function_decl|;
comment|/**      * Should the field query {@link #fieldQuery(String)} be used when detecting this      * field in query string.      */
DECL|method|useFieldQueryWithQueryString
name|boolean
name|useFieldQueryWithQueryString
parameter_list|()
function_decl|;
DECL|method|fieldQuery
name|Query
name|fieldQuery
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
DECL|method|fieldFilter
name|Filter
name|fieldFilter
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
comment|/**      * Constructs a range query based on the mapper.      */
DECL|method|rangeQuery
name|Query
name|rangeQuery
parameter_list|(
name|String
name|lowerTerm
parameter_list|,
name|String
name|upperTerm
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|)
function_decl|;
comment|/**      * Constructs a range query filter based on the mapper.      */
DECL|method|rangeFilter
name|Filter
name|rangeFilter
parameter_list|(
name|String
name|lowerTerm
parameter_list|,
name|String
name|upperTerm
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|)
function_decl|;
DECL|method|sortType
name|int
name|sortType
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

