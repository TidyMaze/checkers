/*******************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.deeplearning4j;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.DataSetIteratorSplitter;
import org.deeplearning4j.datasets.iterator.RandomDataSetIterator;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by agibsonccc on 9/16/15.
 */
public class CheckersNNApp {
    private static final Logger log = LoggerFactory.getLogger(CheckersNNApp.class);

    public static void main(String[] args) throws Exception {
        int nEpochs = 50; // Number of training epochs
        int seed = 123; //

        /*
            Create an iterator using the batch size for one iteration
         */
        log.info("Load data....");
        RecordReader rr = new CSVRecordReader(0, ',');
        rr.initialize(new FileSplit(new File("in/dump.txt")));

        RecordReaderDataSetIterator recordReaderDataSetIterator = new RecordReaderDataSetIterator(rr, 10, 64, 64, true);
        DataNormalization dataNormalization = new NormalizerStandardize();
        dataNormalization.fit(recordReaderDataSetIterator);
        recordReaderDataSetIterator.setPreProcessor(dataNormalization);

        DataSetIteratorSplitter recordReaderDataSetSplittedIterator = new DataSetIteratorSplitter(recordReaderDataSetIterator, 1000, 0.75);

        DataSetIterator trainDataSet = recordReaderDataSetSplittedIterator.getTrainIterator();
        DataSetIterator testDataSet = recordReaderDataSetSplittedIterator.getTestIterator();

        /*
            Construct the neural network
         */
        log.info("Build model....");

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0005)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.01))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .activation(Activation.IDENTITY)
                        .nIn(64)
                        .nOut(32)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(32)
                        .nOut(16)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                        .nIn(16)
                        .nOut(1)
                        .activation(Activation.IDENTITY)
                        .build())
                .backpropType(BackpropType.Standard)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();


        log.info("Train model....");
        model.setListeners(new ScoreIterationListener(1)); //Print score every 10 iterations
        for( int i=0; i<nEpochs; i++ ) {
            model.fit(trainDataSet);
            log.info("*** Completed epoch {} ***", i);

            log.info("Evaluate model....");
            RegressionEvaluation eval = model.evaluateRegression(testDataSet);
            log.info(eval.stats());
            testDataSet.reset();
        }
        log.info("****************Example finished********************");
    }
}
