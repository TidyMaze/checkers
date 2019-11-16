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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
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
        int nEpochs = 10000; // Number of training epochs
        int seed = 123; //

        File locationToSave = new File("../out/model.zip");
        File dataSetFile = new File("../out/dump.txt");

        boolean loadExisting = true;
        boolean train = false;

        MultiLayerNetwork model = null;

        if (loadExisting && locationToSave.exists()) {
            System.out.println("Loading existing model at " + locationToSave.getPath());
            model = MultiLayerNetwork.load(locationToSave, true);
        } else {
            log.info("Building new model");
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
//                .dropOut(0.1)
//                .l2(0.001)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Adam(0.01))
                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(64).nOut(10).activation(Activation.TANH).build())
                    .layer(1, new DenseLayer.Builder().nIn(10).nOut(8).activation(Activation.TANH).build())
                    .layer(2, new DenseLayer.Builder().nIn(8).nOut(8).activation(Activation.TANH).build())
                    .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).nIn(8).nOut(1).activation(Activation.IDENTITY).build())
                    .backpropType(BackpropType.Standard)
                    .build();

            model = new MultiLayerNetwork(conf);
            model.init();
        }

        if (train) {
            log.info("Load dataset at " + dataSetFile.getPath());
            RecordReader rr = new CSVRecordReader(0, ',');
            rr.initialize(new FileSplit(dataSetFile));

            RecordReaderDataSetIterator recordReaderDataSetIterator = new RecordReaderDataSetIterator(rr, 10000, 64, 64, true);
            DataSet allData = recordReaderDataSetIterator.next();
            allData.shuffle();

            SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.90);

            org.nd4j.linalg.dataset.DataSet trainingData = testAndTrain.getTrain();
            org.nd4j.linalg.dataset.DataSet testData = testAndTrain.getTest();

            DataNormalization dataNormalization = new NormalizerStandardize();
            dataNormalization.fit(trainingData);
            dataNormalization.transform(trainingData);
            dataNormalization.transform(testData);

            log.info("Train model");
            model.setListeners(new ScoreIterationListener(10)); //Print score every 10 iterations
            for (int i = 0; i < nEpochs; i++) {
                model.fit(trainingData);
                log.info("*** Completed epoch {} ***", i);

                log.info("Evaluate model....");
                RegressionEvaluation eval = new RegressionEvaluation();
                INDArray output = model.output(testData.getFeatures());
                eval.eval(testData.getLabels(), output);
                log.info(eval.stats());
            }
        } else {
            System.out.println("Training is disabled");
        }

        System.out.println("Saving model to " + locationToSave.getPath());
        model.save(locationToSave);

        // Test the evaluation of a state
        final INDArray input = Nd4j.create(new double[]{
                0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, -1, 0
        }, 1, 64); // expected Output: 0,7809; Desired output: 0,792;

        long startTime = System.nanoTime();

        INDArray out = model.output(input, false);
        long endTime = System.nanoTime();

        // get difference of two nanoTime values
        long timeElapsed = endTime - startTime;

        System.out.println("Execution time in milliseconds : " +
                timeElapsed / 1000000);

        System.out.println("predicted score " + out);
    }


}
