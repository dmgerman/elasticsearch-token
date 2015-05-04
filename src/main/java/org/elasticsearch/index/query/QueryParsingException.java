begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|xcontent
operator|.
name|XContentBuilder
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
name|XContentLocation
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
name|index
operator|.
name|Index
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
name|IndexException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|QueryParsingException
specifier|public
class|class
name|QueryParsingException
extends|extends
name|IndexException
block|{
DECL|field|UNKNOWN_POSITION
specifier|static
specifier|final
name|int
name|UNKNOWN_POSITION
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|lineNumber
specifier|private
name|int
name|lineNumber
init|=
name|UNKNOWN_POSITION
decl_stmt|;
DECL|field|columnNumber
specifier|private
name|int
name|columnNumber
init|=
name|UNKNOWN_POSITION
decl_stmt|;
DECL|method|QueryParsingException
specifier|public
name|QueryParsingException
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|String
name|msg
parameter_list|)
block|{
name|this
argument_list|(
name|parseContext
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|QueryParsingException
specifier|public
name|QueryParsingException
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
name|msg
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|XContentLocation
name|location
init|=
name|parser
operator|.
name|getTokenLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
name|lineNumber
operator|=
name|location
operator|.
name|lineNumber
expr_stmt|;
name|columnNumber
operator|=
name|location
operator|.
name|columnNumber
expr_stmt|;
block|}
block|}
block|}
comment|/**      * This constructor is provided for use in unit tests where a      * {@link QueryParseContext} may not be available      */
DECL|method|QueryParsingException
name|QueryParsingException
parameter_list|(
name|Index
name|index
parameter_list|,
name|int
name|line
parameter_list|,
name|int
name|col
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|msg
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|this
operator|.
name|lineNumber
operator|=
name|line
expr_stmt|;
name|this
operator|.
name|columnNumber
operator|=
name|col
expr_stmt|;
block|}
comment|/**      * Line number of the location of the error      *       * @return the line number or -1 if unknown      */
DECL|method|getLineNumber
specifier|public
name|int
name|getLineNumber
parameter_list|()
block|{
return|return
name|lineNumber
return|;
block|}
comment|/**      * Column number of the location of the error      *       * @return the column number or -1 if unknown      */
DECL|method|getColumnNumber
specifier|public
name|int
name|getColumnNumber
parameter_list|()
block|{
return|return
name|columnNumber
return|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|RestStatus
operator|.
name|BAD_REQUEST
return|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|protected
name|void
name|innerToXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|lineNumber
operator|!=
name|UNKNOWN_POSITION
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"line"
argument_list|,
name|lineNumber
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"col"
argument_list|,
name|columnNumber
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|innerToXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

