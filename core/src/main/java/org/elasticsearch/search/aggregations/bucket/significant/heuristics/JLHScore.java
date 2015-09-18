begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.significant.heuristics
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|significant
operator|.
name|heuristics
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|ParseFieldMatcher
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|XContentParser
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
name|ParsingException
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
DECL|class|JLHScore
specifier|public
class|class
name|JLHScore
extends|extends
name|SignificanceHeuristic
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|JLHScore
name|INSTANCE
init|=
operator|new
name|JLHScore
argument_list|()
decl_stmt|;
DECL|field|NAMES
specifier|protected
specifier|static
specifier|final
name|String
index|[]
name|NAMES
init|=
block|{
literal|"jlh"
block|}
decl_stmt|;
DECL|method|JLHScore
specifier|private
name|JLHScore
parameter_list|()
block|{}
DECL|field|STREAM
specifier|public
specifier|static
specifier|final
name|SignificanceHeuristicStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|SignificanceHeuristicStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|SignificanceHeuristic
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAMES
index|[
literal|0
index|]
return|;
block|}
block|}
decl_stmt|;
DECL|method|readFrom
specifier|public
specifier|static
name|SignificanceHeuristic
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|INSTANCE
return|;
block|}
comment|/**      * Calculates the significance of a term in a sample against a background of      * normal distributions by comparing the changes in frequency. This is the heart      * of the significant terms feature.      */
annotation|@
name|Override
DECL|method|getScore
specifier|public
name|double
name|getScore
parameter_list|(
name|long
name|subsetFreq
parameter_list|,
name|long
name|subsetSize
parameter_list|,
name|long
name|supersetFreq
parameter_list|,
name|long
name|supersetSize
parameter_list|)
block|{
name|checkFrequencyValidity
argument_list|(
name|subsetFreq
argument_list|,
name|subsetSize
argument_list|,
name|supersetFreq
argument_list|,
name|supersetSize
argument_list|,
literal|"JLHScore"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|subsetSize
operator|==
literal|0
operator|)
operator|||
operator|(
name|supersetSize
operator|==
literal|0
operator|)
condition|)
block|{
comment|// avoid any divide by zero issues
return|return
literal|0
return|;
block|}
if|if
condition|(
name|supersetFreq
operator|==
literal|0
condition|)
block|{
comment|// If we are using a background context that is not a strict superset, a foreground
comment|// term may be missing from the background, so for the purposes of this calculation
comment|// we assume a value of 1 for our calculations which avoids returning an "infinity" result
name|supersetFreq
operator|=
literal|1
expr_stmt|;
block|}
name|double
name|subsetProbability
init|=
operator|(
name|double
operator|)
name|subsetFreq
operator|/
operator|(
name|double
operator|)
name|subsetSize
decl_stmt|;
name|double
name|supersetProbability
init|=
operator|(
name|double
operator|)
name|supersetFreq
operator|/
operator|(
name|double
operator|)
name|supersetSize
decl_stmt|;
comment|// Using absoluteProbabilityChange alone favours very common words e.g. you, we etc
comment|// because a doubling in popularity of a common term is a big percent difference
comment|// whereas a rare term would have to achieve a hundred-fold increase in popularity to
comment|// achieve the same difference measure.
comment|// In favouring common words as suggested features for search we would get high
comment|// recall but low precision.
name|double
name|absoluteProbabilityChange
init|=
name|subsetProbability
operator|-
name|supersetProbability
decl_stmt|;
if|if
condition|(
name|absoluteProbabilityChange
operator|<=
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
comment|// Using relativeProbabilityChange tends to favour rarer terms e.g.mis-spellings or
comment|// unique URLs.
comment|// A very low-probability term can very easily double in popularity due to the low
comment|// numbers required to do so whereas a high-probability term would have to add many
comment|// extra individual sightings to achieve the same shift.
comment|// In favouring rare words as suggested features for search we would get high
comment|// precision but low recall.
name|double
name|relativeProbabilityChange
init|=
operator|(
name|subsetProbability
operator|/
name|supersetProbability
operator|)
decl_stmt|;
comment|// A blend of the above metrics - favours medium-rare terms to strike a useful
comment|// balance between precision and recall.
return|return
name|absoluteProbabilityChange
operator|*
name|relativeProbabilityChange
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|JLHScoreParser
specifier|public
specifier|static
class|class
name|JLHScoreParser
implements|implements
name|SignificanceHeuristicParser
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|SignificanceHeuristic
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|ParsingException
block|{
comment|// move to the closing bracket
if|if
condition|(
operator|!
name|parser
operator|.
name|nextToken
argument_list|()
operator|.
name|equals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse [jhl] significance heuristic. expected an empty object, but found [{}] instead"
argument_list|,
name|parser
operator|.
name|currentToken
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|new
name|JLHScore
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getNames
specifier|public
name|String
index|[]
name|getNames
parameter_list|()
block|{
return|return
name|NAMES
return|;
block|}
block|}
DECL|class|JLHScoreBuilder
specifier|public
specifier|static
class|class
name|JLHScoreBuilder
implements|implements
name|SignificanceHeuristicBuilder
block|{
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|builder
operator|.
name|startObject
argument_list|(
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
block|}
end_class

end_unit

