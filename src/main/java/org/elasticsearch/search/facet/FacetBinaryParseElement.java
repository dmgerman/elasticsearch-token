begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|Inject
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentParser
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FacetBinaryParseElement
specifier|public
class|class
name|FacetBinaryParseElement
extends|extends
name|FacetParseElement
block|{
annotation|@
name|Inject
DECL|method|FacetBinaryParseElement
specifier|public
name|FacetBinaryParseElement
parameter_list|(
name|FacetParsers
name|facetParsers
parameter_list|)
block|{
name|super
argument_list|(
name|facetParsers
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|void
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|byte
index|[]
name|facetSource
init|=
name|parser
operator|.
name|binaryValue
argument_list|()
decl_stmt|;
try|try
init|(
name|XContentParser
name|fSourceParser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|facetSource
argument_list|)
operator|.
name|createParser
argument_list|(
name|facetSource
argument_list|)
init|)
block|{
name|fSourceParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
comment|// move past the first START_OBJECT
name|super
operator|.
name|parse
argument_list|(
name|fSourceParser
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

