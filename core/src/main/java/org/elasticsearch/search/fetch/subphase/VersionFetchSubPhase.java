begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.subphase
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
name|index
operator|.
name|NumericDocValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|uid
operator|.
name|Versions
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
name|VersionFieldMapper
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|VersionFetchSubPhase
specifier|public
specifier|final
class|class
name|VersionFetchSubPhase
implements|implements
name|FetchSubPhase
block|{
annotation|@
name|Override
DECL|method|hitExecute
specifier|public
name|void
name|hitExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|HitContext
name|hitContext
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|version
argument_list|()
operator|==
literal|false
operator|||
operator|(
name|context
operator|.
name|storedFieldsContext
argument_list|()
operator|!=
literal|null
operator|&&
name|context
operator|.
name|storedFieldsContext
argument_list|()
operator|.
name|fetchFields
argument_list|()
operator|==
literal|false
operator|)
condition|)
block|{
return|return;
block|}
name|long
name|version
init|=
name|Versions
operator|.
name|NOT_FOUND
decl_stmt|;
try|try
block|{
name|NumericDocValues
name|versions
init|=
name|hitContext
operator|.
name|reader
argument_list|()
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|versions
operator|!=
literal|null
condition|)
block|{
name|version
operator|=
name|versions
operator|.
name|get
argument_list|(
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Could not retrieve version"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|version
argument_list|(
name|version
operator|<
literal|0
condition|?
operator|-
literal|1
else|:
name|version
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
